package com.easy.id.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "easy-id-generator.snowflake")
@Data
public class SnowflakeProperties {

    /**
     * 是否开启雪花算法
     */
    private boolean enable = false;

    private ZK zk;

    /**
     * 当zk不可访问时，从本地文件中读取之前备份的workerId
     */
    private boolean loadWorkerIdFromFileWhenZkDown = true;

    @Data
    public static class ZK {
        /**
         * zk链接，多个用,分隔
         */
        private String connectionString;
    }
}
