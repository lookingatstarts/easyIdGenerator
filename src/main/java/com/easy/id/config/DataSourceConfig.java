package com.easy.id.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangbingbing
 * @version 1.0.0
 * @createTime 2020年05月29日
 */
@Configuration
public class DataSourceConfig {

    @Value("${easy-id.db.name:primary}")
    private String dbName;

    @Bean
    public DataSource dynamicDataSource() {
        String[] dbNames = dbName.split(",");
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        Map<Object, Object> dataSourceList = new HashMap<>(dbNames.length);
        dynamicDataSource.setTargetDataSources(dataSourceList);
        dynamicDataSource.setDataSourceKeys(dbNames);
        for (String db : dbNames) {
            HikariConfig config = new HikariConfig("/db-" + db + ".properties");
            dataSourceList.put(db, new HikariDataSource(config));
        }
        return dynamicDataSource;
    }
}
