package com.easy.id.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhangbingbing
 * @version 1.0.0
 * @createTime 2020年06月02日
 */
@Configuration
public class BeanConfig {

    @Bean
    @Module(value = "snowflake.enable")
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

    @Bean
    @Module(value = "segment.enable")
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

}
