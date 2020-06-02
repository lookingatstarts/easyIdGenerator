package com.easy.id.service;

import com.easy.id.exception.SystemClockCallbackException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @author zhangbingbing
 * @version 1.0.0
 * @Description 雪花算法实现
 * @createTime 2020年06月01日
 */
@Service
@ConditionalOnBean(name = "snowflakeZKHolder")
public class SnowflakeEasyIdService implements EasyIdService {
    /**
     * 2020-06-01 00:00:00 (UTC+8)
     */
    private final long startAt = 1590940800000L;
    private final int workIdBits = 10;
    private final int maxWorkId = ~(-1 << workIdBits);
    private final int sequenceBits = 12;
    private final int workIdShift = sequenceBits;
    private final int timestampShift = workIdBits + sequenceBits;
    private final int sequenceMask = ~(-1 << sequenceBits);
    private final Random random = new Random();
    private int sequence = 0;
    private long lastTimestamp = -1L;
    // todo
    private Integer workId;

    private synchronized long nextId() {
        long now = now();
        // 时钟回调了
        if (now < lastTimestamp) {
            long offset = lastTimestamp - now;
            if (offset > 5) {
                throw new SystemClockCallbackException("system clock callback slow " + offset);
            }
            try {
                this.wait(offset << 1);
            } catch (InterruptedException e) {
                throw new SystemClockCallbackException("system clock callback slow " + offset, e);
            }
        }
        if (now == lastTimestamp) {
            sequence = (sequence + 1) & sequenceMask;
            // 该毫秒内的sequence已经用完了
            if (sequence == 0) {
                sequence = random.nextInt(100);
                now = tillNextMill(lastTimestamp);
            }
        }
        // 从新的毫秒开始
        if (now > lastTimestamp) {
            sequence = random.nextInt(100);
        }
        lastTimestamp = now;
        return toId(lastTimestamp, workId, sequence);
    }

    private synchronized Set<Long> nextIds(int batchSize) {
        if ((batchSize & sequenceMask) == 0) {
            throw new IllegalArgumentException("batch size " + batchSize);
        }
        long now = now();
        if (now < lastTimestamp) {
            long offset = lastTimestamp - now;
            if (offset > 5) {
                throw new SystemClockCallbackException("system clock callback slow " + offset);
            }
            try {
                this.wait(offset << 1);
            } catch (InterruptedException e) {
                throw new SystemClockCallbackException("system clock callback slow " + offset, e);
            }
        }
        Set<Long> nextIds = new HashSet<>(batchSize);
        while (nextIds.size() < batchSize) {
            // 在本毫秒
            if (now == lastTimestamp) {
                sequence = (sequence + 1) & sequenceMask;
                // 本毫秒内的sequence用完了
                if (sequence == 0) {
                    sequence = random.nextInt(100);
                    now = tillNextMill(lastTimestamp);
                }
                nextIds.add(toId(now, workId, sequence));
                continue;
            }
            // 在新的毫秒
            if (now > lastTimestamp) {
                sequence = random.nextInt(100);
                int loop = batchSize - nextIds.size();
                for (int i = 0; i < loop; i++) {
                    sequence = sequence + 1;
                    nextIds.add(toId(now, workId, sequence));
                }
            }
        }
        lastTimestamp = now;
        return nextIds;
    }

    private long toId(long timestamp, int workId, int sequence) {
        return ((timestamp - startAt) << timestampShift) | (workId << workIdShift) | sequence;
    }

    private long tillNextMill(long lastTimestamp) {
        long now = now();
        while (now <= lastTimestamp) {
            now = now();
        }
        return now;
    }

    private long now() {
        return System.currentTimeMillis();
    }

    @Override
    public Long getNextId(String businessType) {
        return nextId();
    }

    @Override
    public Set<Long> getNextIdBatch(String businessType, int batchSize) {
        return nextIds(batchSize);
    }
}
