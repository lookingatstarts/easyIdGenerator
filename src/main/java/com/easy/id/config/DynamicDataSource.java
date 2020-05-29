package com.easy.id.config;

import lombok.Setter;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.Random;

/**
 * @author zhangbingbing
 * @version 1.0.0
 * @createTime 2020年05月29日
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    private final Random r = new Random();
    @Setter
    private String[] dataSourceKeys;

    @Override
    protected Object determineCurrentLookupKey() {
        if (dataSourceKeys.length == 1) {
            return dataSourceKeys[0];
        }
        return dataSourceKeys[r.nextInt(dataSourceKeys.length)];
    }
}
