package com.easy.id.generator;

import com.easy.id.entity.Segment;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 号段
 */
public class SegmentId {

    /**
     * 该号段最大支持的ID
     */
    private long maxId;

    private long loadingNextSegmentAt;

    private AtomicLong currentId;

    /**
     * 每次增长量
     */
    private int increment;

    /**
     * 模数
     */
    private int mod;
    private volatile boolean hasInit;

    public SegmentId(Segment segment) {
        maxId = segment.getMaxId();
        currentId = new AtomicLong(segment.getMaxId() - segment.getStep());
        // 当该号段30%的id被使用完时，开始异步加载下一个号段
        loadingNextSegmentAt = currentId.get() + (segment.getStep() * 3 / 10);
        increment = segment.getIncrement();
        mod = segment.getMod();
        init();
    }

    private void init() {
        if (hasInit) {
            return;
        }
        synchronized (this) {
            if (hasInit) {
                return;
            }
            long id = currentId.get();
            if (id % increment == mod) {
                hasInit = true;
                return;
            }
            for (int i = 0; i <= increment; i++) {
                id = currentId.incrementAndGet();
                if (id % increment == mod) {
                    currentId.addAndGet(-increment);
                    hasInit = true;
                    return;
                }
            }
        }
    }
}
