package com.easy.id.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "easy-id-generator.segment")
@Data
public class SegmentProperties {

    /**
     * 是否开启号段算法
     */
    private boolean enable = false;

    /**
     * 数据配置文件
     */
    private List<String> dbList;

    /**
     * 从数据库获取号段失败重试次数
     */
    private Integer fetchSegmentRetryTimes = 3;
}
