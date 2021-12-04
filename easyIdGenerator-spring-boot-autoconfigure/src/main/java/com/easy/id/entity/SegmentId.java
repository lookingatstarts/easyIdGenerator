package com.easy.id.entity;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 号段
 */
public class SegmentId {

    /**
     * 该号段最大支持的ID
     */
    private final long maxId;
    /**
     * 每次增长量
     */
    private final int increment;
    /**
     * 模数
     */
    private final int remainder;
    /**
     * 当前id超过这个数时，开始异步加载下个号段
     */
    private final long loadingNextSegmentAt;

    private final AtomicLong currentId;

    private volatile boolean hasInit;

    public SegmentId(Segment segment) {
        this.maxId = segment.getMaxId();
        this.currentId = new AtomicLong(segment.getMaxId() - segment.getStep());
        // 当该号段30%的id被使用完时，开始异步加载下一个号段
        this.loadingNextSegmentAt = currentId.get() + (segment.getStep() * 3L / 10);
        this.increment = segment.getIncrement();
        this.remainder = segment.getRemainder();
        init();
    }

    public boolean useful() {
        return currentId.get() <= maxId;
    }

    public Result nextId() {
        init();
        long id = currentId.addAndGet(increment);
        if (id > maxId) {
            return new Result(ResultCode.OVER, null);
        }
        if (id >= loadingNextSegmentAt) {
            return new Result(ResultCode.SHOULD_LOADING_NEXT_SEGMENT, id);
        }
        return new Result(ResultCode.NORMAL, id);
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
            if ((id % increment) == remainder) {
                hasInit = true;
                return;
            }
            for (int i = 0; i <= increment; i++) {
                id = currentId.incrementAndGet();
                if ((id % increment) == remainder) {
                    currentId.addAndGet(-increment);
                    hasInit = true;
                    return;
                }
            }
        }
    }
}
