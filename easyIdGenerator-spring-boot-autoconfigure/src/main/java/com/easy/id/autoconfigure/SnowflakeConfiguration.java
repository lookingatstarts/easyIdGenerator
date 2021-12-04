package com.easy.id.autoconfigure;


import com.easy.id.service.snowflake.Snowflake;
import com.easy.id.service.snowflake.SnowflakeZKHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@EnableConfigurationProperties(SnowflakeProperties.class)
@ConditionalOnProperty(value = "easy-id-generator.snowflake", havingValue = "true")
public class SnowflakeConfiguration {

    @Autowired
    private SnowflakeProperties properties;

    @Bean
    public SnowflakeZKHolder snowflakeZKHolder() {
        AtomicInteger threadIncr = new AtomicInteger(0);
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(2, (r) -> {
            int incr = threadIncr.incrementAndGet();
            if (incr >= 1000) {
                threadIncr.set(0);
                incr = 1;
            }
            return new Thread(r, "upload-data-to-zk-schedule-thread" + incr);
        }, new ThreadPoolExecutor.CallerRunsPolicy());
        SnowflakeZKHolder snowflakeZKHolder = new SnowflakeZKHolder(scheduledExecutorService, properties);
        snowflakeZKHolder.postConstruct();
        return snowflakeZKHolder;
    }

    @Bean
    public Snowflake snowflake(SnowflakeZKHolder snowflakeZKHolder) {
        return new Snowflake(snowflakeZKHolder.getWorkerID());
    }
}
