package com.easy.id.entity;

import lombok.Data;

/**
 * 假设有10台机器，那么他们的increment应该都为10，mod分别为0-9或者1-10
 */
@Data
public class Segment {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 数据库乐观锁
     */
    private Long version;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 当前号段最大的ID
     */
    private Long maxId;

    /**
     * 步长：号段长度
     */
    private Integer step;

    /**
     * 自增量
     */
    private Integer increment;

    /**
     * 模数
     */
    private Integer remainder;

    /**
     * 创建时间
     */
    private Long createdAt;

    /**
     * 更新时间
     */
    private Long updatedAt;
}
