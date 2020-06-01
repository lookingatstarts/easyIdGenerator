package com.easy.id.exception;

/**
 * @author zhangbingbing
 * @version 1.0.0
 * @Description 系统时间回调异常
 * @createTime 2020年06月01日
 */
public class SystemClockCallbackException extends RuntimeException {

    private static final long serialVersionUID = -6264588182225994225L;

    public SystemClockCallbackException(String msg) {
        super(msg);
    }

    public SystemClockCallbackException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
