package com.easy.id.generator;

import com.easy.id.entity.Result;
import com.easy.id.entity.ResultCode;
import com.easy.id.entity.SegmentId;
import com.easy.id.exception.GetNextIdException;
import com.easy.id.service.SegmentIdService;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CachedIdGenerator implements IdGenerator {

    private final String businessType;
    private final Object lock = new Object();
    private volatile SegmentId currentSegmentId;
    private volatile SegmentId nextSegmentId;
    private volatile boolean isLoadingNextSegment = false;
    private SegmentIdService segmentIdService;
    private AtomicInteger threadIdIncr = new AtomicInteger(1);

    private final ExecutorService executorService = new ThreadPoolExecutor(1, 1, 5, TimeUnit.MINUTES, new SynchronousQueue<>(), r -> {
        if (threadIdIncr.get() > 1000) {
            threadIdIncr.set(1);
        }
        return new Thread(r, "fetch-next-segment-thread-" + threadIdIncr.getAndIncrement());
    }, new ThreadPoolExecutor.CallerRunsPolicy());

    public CachedIdGenerator(String businessType, SegmentIdService segmentIdService) {
        this.businessType = businessType;
        this.segmentIdService = segmentIdService;
    }

    private synchronized void loadCurrent() {
        if (currentSegmentId == null || !currentSegmentId.useful()) {
            // 下个号段没有加载
            if (nextSegmentId == null) {
                currentSegmentId = segmentIdService.fetchNextSegmentId(businessType);
            }
            // 下个号段已经加载过了，直接使用
            if (nextSegmentId != null) {
                currentSegmentId = nextSegmentId;
                nextSegmentId = null;
            }
        }
    }

    private void loadNext() {
        // 下个号段没有被使用||下个号段正在加载
        if (nextSegmentId != null || isLoadingNextSegment) {
            return;
        }
        synchronized (lock) {
            if (nextSegmentId == null && !isLoadingNextSegment) {
                isLoadingNextSegment = true;
                executorService.submit(() -> {
                    try {
                        log.debug("异步加载下个号段");
                        nextSegmentId = segmentIdService.fetchNextSegmentId(businessType);
                    } catch (Exception e) {
                        log.error("异步加载下个号段失败", e);
                    } finally {
                        isLoadingNextSegment = false;
                    }
                });
            }
        }
    }

    @Override
    public Long nextId() {
        while (!Thread.currentThread().isInterrupted()) {
            loadCurrent();
            final Result result = currentSegmentId.nextId();
            final ResultCode code = result.getCode();
            if (code == ResultCode.OVER) {
                loadCurrent();
            }
            if (code == ResultCode.NORMAL) {
                return result.getId();
            }
            // 异步加载下一个号段
            if (code == ResultCode.SHOULD_LOADING_NEXT_SEGMENT) {
                loadNext();
                return result.getId();
            }
        }
        throw new GetNextIdException("get next id fail");
    }

    @Override
    public Set<Long> nextIds(int patchSize) {
        if (patchSize == 1) {
            return Collections.singleton(nextId());
        }
        Set<Long> ids = new HashSet<>(patchSize);
        for (int i = 0; i < patchSize; i++) {
            ids.add(nextId());
        }
        return ids;
    }
}
