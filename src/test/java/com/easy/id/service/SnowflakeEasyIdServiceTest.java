package com.easy.id.service;

import com.easy.id.service.snowflake.SnowflakeEasyIdService;

import java.util.Set;

/**
 * @author zhangbingbing
 * @version 1.0.0
 * @Description SnowflakeEasyIdService测试用例
 * @createTime 2020年06月01日
 */
class SnowflakeEasyIdServiceTest {

    public static void main(String[] args) {
        SnowflakeEasyIdService service = new SnowflakeEasyIdService();
        int batchSize = 1000;
        Set<Long> nextIdBatch = service.getNextIdBatch("", batchSize);
        System.out.println(batchSize == nextIdBatch.size());
    }
}