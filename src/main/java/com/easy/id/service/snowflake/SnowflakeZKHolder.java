package com.easy.id.service.snowflake;

import com.alibaba.fastjson.JSON;
import com.easy.id.config.Module;
import com.easy.id.exception.SystemClockCallbackException;
import com.easy.id.util.IPUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Module(value = "snowflake.enable")
public class SnowflakeZKHolder {

    private static final String SPLIT = "-";
    /**
     * 保存所有数据持久的节点
     */
    private static final String ZK_PATH = "/easy-id-generator/snowflake/forever";
    /**
     * 持久化workerId，文件存放位置
     */
    private static final String DUMP_PATH = "workerID/workerID.properties";

    @Autowired
    @Qualifier("updateDataToZKScheduledExecutorService")
    private ScheduledExecutorService scheduledExecutorService;

    @Value("${easy-id-generator.snowflake.load-worker-id-from-file-when-zk-down:true}")
    private boolean loadWorkerIdFromFileWhenZkDown;
    /**
     * 本机地址
     */
    private String localIp;
    /**
     * 本机端口
     */
    @Value("${server.port}")
    private String localPort;
    /**
     * zk连接地址
     * eg: ip1:port1,ip2:port2
     */
    @Value("${easy-id-generator.snowflake.zk.connection-string}")
    private String zkConnectionString;
    private Integer workerId;
    /**
     * 上次更新数据时间
     */
    private long lastUpdateAt;

    private volatile boolean hasInitFinish = false;

    @PostConstruct
    public void postConstruct() {
        try {
            init();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public int getWorkerID() {
        if (hasInitFinish) {
            return workerId;
        }
        throw new IllegalStateException("worker id not init");
    }

    private void init() throws Exception {
        try {
            localIp = IPUtil.getHostAddress();
            String localZKPath = ZK_PATH + "/" + localIp + ":" + localPort;
            CuratorFramework client = connectToZk();
            client.start();
            final Stat stat = client.checkExists().forPath(ZK_PATH);
            // 不存在根结点，第一次使用，创建根结点
            if (stat == null) {
                // 创建有序永久结点 /easy-id-generator/snowflake/forever/ip:port-xxx,并上传数据
                localZKPath = createPersistentSequentialNode(client, localZKPath, buildData());
                workerId = getWorkerId(localZKPath);
                // 持久化workerId
                updateWorkerId(workerId);
                // 定时上报本机时间到zk
                scheduledUploadTimeToZK(client, localZKPath);
                hasInitFinish = true;
                return;
            }
            // Map<localAddress,workerId>
            Map<String, Integer> localAddressWorkerIdMap = new HashMap<>(16);
            // Map<localAddress,path>
            Map<String, String> localAddressPathMap = new HashMap<>(16);
            for (String key : client.getChildren().forPath(ZK_PATH)) {
                final String[] split = key.split("-");
                localAddressPathMap.put(split[0], key);
                // value=zk有序结点的需要
                localAddressWorkerIdMap.put(split[0], Integer.valueOf(split[1]));
            }
            String localAddress = localIp + ":" + localPort;
            workerId = localAddressWorkerIdMap.get(localAddress);
            if (workerId != null) {
                localZKPath = ZK_PATH + "/" + localAddressPathMap.get(localAddress);
                // 校验时间是否回调
                checkTimestamp(client, localZKPath);
                scheduledUploadTimeToZK(client, localZKPath);
                updateWorkerId(workerId);
                hasInitFinish = true;
                return;
            }
            localZKPath = createPersistentSequentialNode(client, localZKPath, buildData());
            workerId = Integer.parseInt((localZKPath.split("-"))[1]);
            scheduledUploadTimeToZK(client, localZKPath);
            updateWorkerId(workerId);
            hasInitFinish = true;
        } catch (Exception e) {
            if (!loadWorkerIdFromFileWhenZkDown) {
                throw e;
            }
            log.error("can load worker id from zk , try to load worker id from file", e);
            // 从本地文件中读取workerId，如果系统时针回调，可能会出现
            final Integer workerIdFromFile = loadWorkerIdFromFile();
            if (workerIdFromFile != null) {
                workerId = workerIdFromFile;
                hasInitFinish = true;
                return;
            }
            throw e;
        }
    }


    private Integer getWorkerId(String localZKPath) {
        String[] split = localZKPath.split(SPLIT);
        return Integer.parseInt(split[1]);
    }

    /**
     * @return true 检查通过
     */
    private void checkTimestamp(CuratorFramework client, String localZKPath) throws Exception {
        final Endpoint endpoint = JSON.parseObject(new String(client.getData().forPath(localZKPath)), Endpoint.class);
        // 该节点的时间不能大于最后一次上报的时间
        if (endpoint.getTimestamp() > System.currentTimeMillis()) {
            throw new SystemClockCallbackException("system clock callback");
        }
    }

    private void scheduledUploadTimeToZK(CuratorFramework client, String localZKPath) {
        scheduledExecutorService.schedule(() -> {
            // 如果时针回调了就不同步
            if (System.currentTimeMillis() < lastUpdateAt) {
                return;
            }
            try {
                client.setData().forPath(localZKPath, buildData());
                lastUpdateAt = System.currentTimeMillis();
                log.debug("upload time to zk at" + lastUpdateAt);
            } catch (Exception e) {
                log.error("update init data error path is {} error is {}", localZKPath, e);
            }
        }, 5, TimeUnit.SECONDS);
    }

    private Integer loadWorkerIdFromFile() {
        try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(DUMP_PATH)) {
            Properties properties = new Properties();
            properties.load(resourceAsStream);
            final String workerID = properties.getProperty("workerID");
            if (workerID != null) {
                return Integer.parseInt(workerID);
            }
            return null;
        } catch (IOException e) {
            log.error("load worker id from file error", e);
        }
        return null;
    }

    private void updateWorkerId(int workerId) {
        if (!loadWorkerIdFromFileWhenZkDown) {
            return;
        }
        try {
            String classpath = ResourceUtils.getURL("classpath:").getFile();
            File file = new File(classpath + "/" + DUMP_PATH);
            if (!file.exists()) {
                boolean mkdirs = file.getParentFile().mkdirs();
                if (!mkdirs) {
                    log.error("mkdir {} error", file.getParentFile().toString());
                    return;
                }
                log.info("mkdir {}", file.toString());
            }
            Files.write(file.toPath(), ("workerID=" + workerId).getBytes());
        } catch (FileNotFoundException e) {
            log.error("", e);
        } catch (IOException e) {
            log.warn("write workerID to file {} error", DUMP_PATH, e);
        }
    }

    private String createPersistentSequentialNode(CuratorFramework client, String path, byte[] data) throws Exception {
        return client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                .forPath(path + "-", data);
    }

    private CuratorFramework connectToZk() {
        return CuratorFrameworkFactory.builder()
                .connectString(zkConnectionString)
                .retryPolicy(new RetryUntilElapsed((int) TimeUnit.SECONDS.toMillis(5), (int) TimeUnit.SECONDS.toMillis(1)))
                .connectionTimeoutMs((int) TimeUnit.SECONDS.toMillis(10))
                .sessionTimeoutMs((int) TimeUnit.SECONDS.toMillis(6))
                .build();
    }

    private byte[] buildData() {
        return JSON.toJSONString(new Endpoint(localIp, localPort, System.currentTimeMillis())).getBytes();
    }

    /**
     * 上传到zk的数据
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Endpoint {
        private String ip;
        private String port;
        private Long timestamp;
    }
}
