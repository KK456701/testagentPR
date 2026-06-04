package com.study.room.exception;

import com.study.room.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获所有的 Exception 获取内部具体的错误信息
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统发生异常，异常信息: ", e);
        // 如果是自定义异常或已知校验异常，这里可以判断 instanceof 或者具体提取
        // 这里简化，直接返回通用的 error
        return Result.error(e.getMessage() != null ? e.getMessage() : "系统繁忙，请稍后再试");
    }
}