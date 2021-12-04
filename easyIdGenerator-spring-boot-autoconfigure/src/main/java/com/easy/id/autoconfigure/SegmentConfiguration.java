package com.easy.id.autoconfigure;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@EnableConfigurationProperties(value = SegmentProperties.class)
@ConditionalOnProperty(value = "easy-id-generator.segment.enable", havingValue = "true")
public class SegmentConfiguration {

    @Autowired
    private SegmentProperties properties;

    @Bean
    public ExecutorService fetchNextSegmentExecutor() {
        AtomicInteger threadIncr = new AtomicInteger(0);
        return new ThreadPoolExecutor(1, 2, 5, TimeUnit.MINUTES, new SynchronousQueue<>(), r -> {
            int incr = threadIncr.incrementAndGet();
            if (incr >= 1000) {
                threadIncr.set(0);
                incr = 1;
            }
            return new Thread(r, "fetch-next-segment-thread-" + incr);
        }, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean
    public DynamicDataSource dynamicDataSource() {
        List<String> dbList = properties.getDbList();
        // 初始化数据库
        List<HikariDataSource> hikariDataSourceList = new ArrayList<>(dbList.size());
        for (String db : dbList) {
            HikariConfig config = new HikariConfig("/" + db + ".properties");
            hikariDataSourceList.add(new HikariDataSource(config));
        }
        return new DynamicDataSource(hikariDataSourceList);
    }

    public static class DynamicDataSource {

        private final List<HikariDataSource> hikariDataSourceList;
        // 同一个线程共用一个数据库连接
        private final ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();

        public DynamicDataSource(List<HikariDataSource> hikariDataSourceList) {
            this.hikariDataSourceList = hikariDataSourceList;
        }

        public Connection getConnection() throws SQLException {
            Connection connection = connectionThreadLocal.get();
            if (connection != null) {
                return connection;
            }
            connection = hikariDataSourceList.get(new Random().nextInt(hikariDataSourceList.size())).getConnection();
            // 设置隔离级别为RC
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connectionThreadLocal.set(connection);
            return connection;
        }

        public void releaseConnection() throws SQLException {
            final Connection connection = connectionThreadLocal.get();
            if (connection != null) {
                connectionThreadLocal.remove();
                connection.close();
            }
        }
    }
}
