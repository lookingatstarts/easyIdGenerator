package com.easy.id.exception;

public class FetchSegmentFailException extends RuntimeException {

    private static final long serialVersionUID = -6997616606690545563L;

    public FetchSegmentFailException(String msg) {
        super(msg);
    }

    public FetchSegmentFailException(Throwable cause) {
        super(cause);
    }
}
