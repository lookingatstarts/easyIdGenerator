package com.easy.id.config;

import com.easy.id.web.resp.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ApiResponse<Void> handleException(Throwable e) {
        log.error(e.getMessage(), e);
        return ApiResponse.exception(e);
    }
}
