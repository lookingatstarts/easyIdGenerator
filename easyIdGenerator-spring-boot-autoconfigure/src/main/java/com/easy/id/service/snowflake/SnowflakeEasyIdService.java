package com.easy.id.service.snowflake;

import com.easy.id.autoconfigure.SnowflakeConfiguration;
import com.easy.id.service.EasyIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @author zhangbingbing
 * @version 1.0.0
 * @Description 雪花算法实现
 * @createTime 2020年06月01日
 */
@Service
@ConditionalOnBean(SnowflakeConfiguration.class)
public class SnowflakeEasyIdService implements EasyIdService {

    @Autowired
    private Snowflake snowflake;

    @Override
    public Long getNextId(String businessType) {
        return snowflake.nextId();
    }

    @Override
    public Set<Long> getNextIdBatch(String businessType, int batchSize) {
        return snowflake.nextIds(batchSize);
    }
}
