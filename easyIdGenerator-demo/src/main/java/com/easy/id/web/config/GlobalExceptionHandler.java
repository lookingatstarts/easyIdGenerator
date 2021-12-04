package com.easy.id.web.config;

import com.easy.id.web.resp.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ApiResponse<Void> handleException(Throwable e) {
        log.error(e.getMessage(), e);
        return ApiResponse.exception(e);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ApiResponse<Void> handleRuntimeException(RuntimeException e) {
        log.error(e.getMessage(), e);
        return ApiResponse.exception(e);
    }

    @ExceptionHandler(BindException.class)
    @ResponseBody
    public ApiResponse<Void> BindExceptionHandler(BindException e) {
        log.error("系统错误", e);
        String message = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining());
        return ApiResponse.error(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ApiResponse<Void> ConstraintViolationExceptionHandler(ConstraintViolationException e) {
        log.error("系统错误", e);
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining());
        return ApiResponse.error(message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ApiResponse<Void> MethodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        log.error("系统错误", e);
        String message = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining());
        return ApiResponse.error(message);
    }
}
