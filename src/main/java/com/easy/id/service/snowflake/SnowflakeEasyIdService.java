package com.easy.id.service.snowflake;

import com.easy.id.config.Module;
import com.easy.id.service.EasyIdService;
import java.util.Set;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhangbingbing
 * @version 1.0.0
 * @Description 雪花算法实现
 * @createTime 2020年06月01日
 */
@Service
@Module("snowflake.enable")
@Slf4j
public class SnowflakeEasyIdService implements EasyIdService {

    @Autowired
    private SnowflakeZKHolder snowflakeZKHolder;
    private Snowflake snowflake;

    @PostConstruct
    public void init() {
        snowflake = new Snowflake(snowflakeZKHolder.getWorkerID());
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
