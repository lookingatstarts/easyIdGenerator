package com.easy.id.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhangbingbing
 * @version 1.0.0
 * @createTime 2020年06月02日
 */
@Configuration
public class BeanConfig {

    @Bean
    @ConditionalOnBean(name = "snowflakeEasyIdService")
    public ScheduledExecutorService updateDataToZKScheduledExecutorService() {
        AtomicInteger threadIncr = new AtomicInteger(0);
        return new ScheduledThreadPoolExecutor(2, (r) -> {
            int incr = threadIncr.incrementAndGet();
            if (incr >= 1000) {
                threadIncr.set(0);
                incr = 1;
            }
            return new Thread(r, "upload-data-to-zk-schedule-thread" + incr);
        }, new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
