package com.easy.id.web.resp;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Data
@Slf4j
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = -5959424433403902244L;

    private String msg = "成功";
    private Boolean success = Boolean.TRUE;
    private T data;

    public static ApiResponse<Void> success() {
        return new ApiResponse<>();
    }

    public static <T> ApiResponse<T> data(T t) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setData(t);
        return response;
    }

    public static ApiResponse<Void> exception(Throwable throwable) {
        log.error("系统错误", throwable);
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(Boolean.FALSE);
        String message = throwable.getMessage();
        if (message == null) {
            Throwable cause = throwable.getCause();
            message = cause.getMessage();
        }
        if (message == null) {
            message = "系统发生错误";
        }
        apiResponse.setMsg(message);
        return apiResponse;
    }
}
