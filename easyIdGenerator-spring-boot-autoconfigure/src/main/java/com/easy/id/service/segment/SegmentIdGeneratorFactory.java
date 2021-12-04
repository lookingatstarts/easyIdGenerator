package com.easy.id.service.segment;

import com.easy.id.service.generator.AbstractIdGeneratorFactory;
import com.easy.id.service.generator.IdGenerator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 生成IdGenerator的工厂
 */
public class SegmentIdGeneratorFactory extends AbstractIdGeneratorFactory {

    private final ExecutorService fetchNextSegmentExecutor;
    private final SegmentIdService segmentIdService;

    public SegmentIdGeneratorFactory(SegmentIdService segmentIdService) {
        this.segmentIdService = segmentIdService;
        fetchNextSegmentExecutor = fetchNextSegmentExecutor();
    }

    @Override
    protected IdGenerator createIdGenerator(String businessType) {
        return new SegmentCachedIdGenerator(fetchNextSegmentExecutor, segmentIdService, businessType);
    }

    protected ExecutorService fetchNextSegmentExecutor() {
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
