package com.easy.id.autoconfigure;

import com.easy.id.service.segment.SegmentEasyIdService;
import com.easy.id.service.segment.SegmentIdGeneratorFactory;
import com.easy.id.service.segment.SegmentIdService;
import com.easy.id.service.segment.SegmentIdServiceImpl;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
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

@Configuration
@EnableConfigurationProperties(value = SegmentProperties.class)
@ConditionalOnProperty(prefix = "easy-id-generator.segment", value = "enable", havingValue = "true")
@Slf4j
public class SegmentAutoConfiguration {

    @Autowired
    private SegmentProperties properties;

    public SegmentAutoConfiguration() {
        log.info("启动号段方式生成ID");
    }

    @Bean
    public SegmentIdService segmentIdService() {
        return new SegmentIdServiceImpl(properties, dynamicDataSource());
    }

    @Bean
    public SegmentIdGeneratorFactory segmentIdGeneratorFactory() {
        return new SegmentIdGeneratorFactory(segmentIdService());
    }

    @Bean
    public SegmentEasyIdService segmentEasyIdService(SegmentIdGeneratorFactory segmentIdGeneratorFactory) {
        return new SegmentEasyIdService(segmentIdGeneratorFactory);
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
