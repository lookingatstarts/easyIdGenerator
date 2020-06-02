package com.easy.id.service.snowflake;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class SnowflakeZKHolder {

    /**
     * 保存所有数据持久的节点
     */
    private final String ZK_PATH = "/easy-id-generator/snowflake/forever";
    /**
     * 持久化workerId，文件存放位置
     */
    private final String DUMP_PATH = "/workerID/dump.properties";
    /**
     * 本机地址
     */
    private final String localIp;
    /**
     * 本机端口
     */
    private final String localPort;
    /**
     * zk连接地址
     * eg: ip1:port1,ip2:port2
     */
    private final String zkConnectionString;
    private final AtomicInteger threadIncr = new AtomicInteger(0);
    private int workerId;
    /**
     * ZK_PATH/ip:port-0000001序列号
     */
    private String localZKPath;
    /**
     * 上次更新数据时间
     */
    private long lastUpdateAt;
    private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, (r) -> {
        int incr = threadIncr.get();
        if (incr >= 1000) {
            threadIncr.set(0);
            incr = 1;
        }
        return new Thread(r, "easy-id-generator-upload-time-zk-schedule-" + incr);
    }, new ThreadPoolExecutor.CallerRunsPolicy());

    public SnowflakeZKHolder(String localIp, String localPort, String zkConnectionString) {
        this.localIp = localIp;
        this.localPort = localPort;
        this.zkConnectionString = zkConnectionString;
        this.localZKPath = ZK_PATH + "/" + localIp + ":" + localPort;
    }


    public boolean init() {
        final CuratorFramework client = connectToZk();
        if (client == null) {
            return false;
        }
        try {
            client.start();
            final Stat stat = client.checkExists().forPath(ZK_PATH);
            // 不存在根结点，第一次使用，创建根结点
            if (stat == null) {
                // /easy-id-generator/snowflake/forever/ip:port-0000000,并上传数据
                localZKPath = createNode(client, localZKPath);
                if (localZKPath == null) {
                    return false;
                }
                workerId = 0;
                updateWorkerId(workerId);
                // 定时上报本机时间到zk
                scheduledUploadTimeToZK(client, localZKPath);
                return true;
            }
            Map<String, Integer> nodeMap = new HashMap<>(16);
            Map<String, String> realNodeMap = new HashMap<>(16);
            for (String key : client.getChildren().forPath(ZK_PATH)) {
                final String[] split = key.split("-");
                realNodeMap.put(split[0], key);
                // value=zk有序结点的需要
                nodeMap.put(split[0], Integer.valueOf(split[1]));
            }
            String localAddress = localIp + ":" + localPort;
            Integer workerId = nodeMap.get(localAddress);
            if (workerId != null) {
                localZKPath = ZK_PATH + "/" + realNodeMap.get(localAddress);
                if (!checkTimestamp(client, localZKPath)) {
                    throw new IllegalStateException("system time check error,forever node timestamp greater than this node time");
                }
                scheduledUploadTimeToZK(client, localZKPath);
                updateWorkerId(workerId);
                return true;
            }
            localZKPath = createNode(client, localZKPath);
            if (localZKPath == null) {
                return false;
            }
            workerId = Integer.parseInt((localZKPath.split("-"))[1]);
            scheduledUploadTimeToZK(client, localZKPath);
            updateWorkerId(workerId);
        } catch (Exception e) {
            log.error("can load worker id from zk", e);
            final Integer workerIdFromFile = loadWorkerIdFromFile();
            if (workerIdFromFile == null) {
                return false;
            }
            workerId = workerIdFromFile;
        }
        return false;
    }


    /**
     * @return true 检查通过
     */
    private boolean checkTimestamp(CuratorFramework client, String localZKPath) {
        try {
            final byte[] bytes = client.getData().forPath(localZKPath);
            final Endpoint endpoint = parseBuildData(new String(bytes));
            // 该节点的时间不能大于最后一次上报的时间
            return !(endpoint.getTimestamp() > System.currentTimeMillis());
        } catch (Exception e) {
            log.error("get data fail for {}", localZKPath, e);
            return false;
        }
    }

    private void scheduledUploadTimeToZK(CuratorFramework client, String path) {
        scheduledExecutorService.schedule(() -> {
            if (System.currentTimeMillis() < lastUpdateAt) {
                return;
            }
            try {
                client.setData().forPath(path, buildData());
                lastUpdateAt = System.currentTimeMillis();
            } catch (Exception e) {
                log.error("update init data error path is {} error is {}", path, e);
            }
        }, 5, TimeUnit.SECONDS);
    }

    private Integer loadWorkerIdFromFile() {
        try {
            final File file = ResourceUtils.getFile("classpath:" + DUMP_PATH);
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            final String workerID = properties.getProperty("workerID");
            if (workerID != null) {
                return Integer.parseInt(workerID);
            }
        } catch (IOException e) {
            log.error("load worker id from file error", e);
        }
        return null;
    }

    private void updateWorkerId(int workerId) {
        String classpath;
        try {
            classpath = ResourceUtils.getURL("classpath:").getFile();
        } catch (FileNotFoundException e) {
            log.error("", e);
            return;
        }
        File file = new File(classpath + DUMP_PATH);
        if (!file.exists()) {
            final boolean mkdirs = file.mkdirs();
            if (!mkdirs) {
                log.error("mkdir {} error", file.toString());
                return;
            }
            log.info("mkdir {}", file.toString());
        }
        try {
            Files.write(file.toPath(), ("workerID=" + workerId).getBytes());
        } catch (IOException e) {
            log.warn("write workerID to file {} error", file.toString(), e);
        }
    }

    private String createNode(CuratorFramework client, String path) {
        try {
            return client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                    .forPath(path + "-", buildData());
        } catch (Exception e) {
            log.error("create node {} error", path, e);
        }
        return null;
    }

    private CuratorFramework connectToZk() {
        try {
            return CuratorFrameworkFactory.builder()
                    .connectString(zkConnectionString)
                    .retryPolicy(new RetryUntilElapsed((int) TimeUnit.SECONDS.toMillis(5), (int) TimeUnit.SECONDS.toMillis(1)))
                    .connectionTimeoutMs((int) TimeUnit.SECONDS.toMillis(10))
                    .sessionTimeoutMs((int) TimeUnit.SECONDS.toMillis(6))
                    .build();
        } catch (Exception e) {
            log.error("connecting to zookeeper fail ", e);
        }
        return null;
    }

    private byte[] buildData() {
        return JSON.toJSONString(new Endpoint(localIp, localPort, System.currentTimeMillis())).getBytes();
    }

    public Endpoint parseBuildData(String json) {
        return JSON.parseObject(json, Endpoint.class);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Endpoint {
        private String ip;
        private String port;
        private Long timestamp;
    }
}
