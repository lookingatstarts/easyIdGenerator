package com.easy.id.exception;

/**
 * 号段未找到
 */
public class SegmentNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 6093487904562917327L;

    public SegmentNotFoundException(String msg) {
        super(msg);
    }
}
