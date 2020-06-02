package com.easy.id.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author zhangbingbing
 * @version 1.0.0
 * @createTime 2020年05月29日
 */
@Configuration
@Module(value = "segment.enable")
@ConfigurationProperties(prefix = "easy-id-generator.segment")
public class DataSourceConfig {

    @Setter
    private List<String> dbList;

    @Bean
    public DynamicDataSource dynamicDataSource() {
        List<HikariDataSource> hikariDataSourceList = new ArrayList<>(dbList.size());
        for (String db : dbList) {
            HikariConfig config = new HikariConfig("/" + db + ".properties");
            hikariDataSourceList.add(new HikariDataSource(config));
        }
        return new DynamicDataSource(hikariDataSourceList);
    }

    public static class DynamicDataSource {

        private List<HikariDataSource> hikariDataSourceList;
        private ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();

        public DynamicDataSource(List<HikariDataSource> hikariDataSourceList) {
            this.hikariDataSourceList = hikariDataSourceList;
        }

        public Connection getConnection() throws SQLException {
            Connection connection = connectionThreadLocal.get();
            if (connection != null) {
                return connection;
            }
            connection = hikariDataSourceList.get(new Random().nextInt(hikariDataSourceList.size())).getConnection();
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
