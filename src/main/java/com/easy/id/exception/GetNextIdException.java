package com.easy.id.exception;

/**
 * 获取下一个id异常
 */
public class GetNextIdException extends RuntimeException {

    private static final long serialVersionUID = -5582536965946613712L;

    public GetNextIdException(String msg) {
        super(msg);
    }
}
