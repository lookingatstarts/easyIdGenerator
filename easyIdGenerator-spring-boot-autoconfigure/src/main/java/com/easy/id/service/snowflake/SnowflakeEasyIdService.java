package com.easy.id.service.snowflake;

import com.easy.id.service.EasyIdService;

import java.util.Set;

/**
 * @author zhangbingbing
 * @version 1.0.0
 * @Description 雪花算法实现
 * @createTime 2020年06月01日
 */
public class SnowflakeEasyIdService implements EasyIdService {

    private final Snowflake snowflake;

    public SnowflakeEasyIdService(Snowflake snowflake) {
        this.snowflake = snowflake;
    }

    @Override
    public Long getNextId(String businessType) {
        return snowflake.nextId();
    }

    @Override
    public Set<Long> getNextIdBatch(String businessType, int batchSize) {
        return snowflake.nextIds(batchSize);
    }
}
