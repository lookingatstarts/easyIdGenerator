package com.easy.id.service.segment;

import com.easy.id.entity.Result;
import com.easy.id.entity.ResultCode;
import com.easy.id.entity.SegmentId;
import com.easy.id.exception.GetNextIdException;
import com.easy.id.service.generator.IdGenerator;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * 不同businessType，拥有不同的IdGenerator
 */
@Slf4j
public class SegmentCachedIdGenerator implements IdGenerator {

    private final ExecutorService fetchNextSegmentExecutor;
    private final SegmentIdService segmentIdService;
    private final String businessType;
    private final Object lock = new Object();
    private volatile SegmentId currentSegmentId;
    private volatile SegmentId nextSegmentId;
    /**
     * 是否异步加载下个号段中
     */
    private volatile boolean isLoadingNextSegment = false;

    public SegmentCachedIdGenerator(ExecutorService fetchNextSegmentExecutor, SegmentIdService segmentIdService, String businessType) {
        this.fetchNextSegmentExecutor = fetchNextSegmentExecutor;
        this.segmentIdService = segmentIdService;
        this.businessType = businessType;
    }

    /**
     * 如果当前号段不存在或者用完了，如果下个号段存在，优先使用下个号段
     */
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
                fetchNextSegmentExecutor.submit(() -> {
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
