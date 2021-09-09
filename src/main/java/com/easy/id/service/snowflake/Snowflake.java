package com.easy.id.service.snowflake;

import com.easy.id.exception.SystemClockCallbackException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Snowflake {

  /**
   * 2020-06-01 00:00:00 (UTC+8)
   */
  private final long startAt = 1590940800000L;
  /**
   * 机器号
   */
  private final int workIdBits = 10;
  /**
   * 序列号
   */
  private final int sequenceBits = 12;
  /**
   * workId偏移量
   */
  private final int workIdShift = sequenceBits;
  /**
   * 时间戳偏移量
   */
  private final int timestampShift = workIdBits + sequenceBits;
  private final int sequenceMask = ~(-1 << sequenceBits);
  private final Random random = new Random();
  private int sequence = 0;
  private long lastTimestamp = -1L;
  /**
   * 最大的机器号
   */
  private final int maxWorkId = ~(-1 << workIdBits);
  private final int workId;

  public Snowflake(int workerID) {
    if (workerID > maxWorkId) {
      throw new IllegalStateException(
          "the work id " + workerID + " greater than max work Id " + maxWorkId);
    }
    workId = workerID;
    log.info("snowflake work id {}", workId);
  }

  public synchronized long nextId() {
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
        throw new SystemClockCallbackException("system clock callback slow " + offset);
      }
    }
    // 同一毫秒内
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

  public synchronized Set<Long> nextIds(int batchSize) {
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

  /**
   * 等待下个毫秒，防止等待期间系统时钟被回调，导致方法一直轮询
   */
  private long tillNextMill(long lastTimestamp) {
    long timestamp;
    long offset;
    while (true) {
      timestamp = now();
      offset = lastTimestamp - timestamp;
      if (offset < 0) {
        return timestamp;
      }
      // 时钟回拨不超过5毫秒
      if (offset < 5) {
        try {
          TimeUnit.MILLISECONDS.sleep(offset);
        } catch (InterruptedException ignore) {
        }
      } else {
        throw new SystemClockCallbackException(
            "timestamp check error,last timestamp " + lastTimestamp + ",now " + timestamp);
      }
    }
  }

  private long toId(long timestamp, int workId, int sequence) {
    return ((timestamp - startAt) << timestampShift) | ((long) workId << workIdShift) | sequence;
  }

  private long now() {
    return System.currentTimeMillis();
  }
}
