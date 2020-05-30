package com.easy.id.entity;

public enum ResultCode {
    /**
     * 号段可以正常使用
     */
    NORMAL(1),

    /**
     * 号段可以正常使用，并且需要异步加载下个号段
     */
    SHOULD_LOADING_NEXT_SEGMENT(2),

    /**
     * 当前号段已使用完
     */
    OVER(3);

    private int code;

    ResultCode(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }
}
