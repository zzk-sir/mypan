package com.zhang.mypan.exception;

import com.zhang.mypan.enums.CodeEnum;
import com.zhang.mypan.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 统一异常处理
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理自定义异常
     */
    @ExceptionHandler(value = MyPanException.class)
    @ResponseBody
    public Result myExceptionHandler(MyPanException e) {
        log.error(e.getMessage(), e);
        return Result.fail(e.getMessage(), e.getCodeEnum());
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result exceptionHandler(Exception e) {
        log.error(e.getMessage(), e);
        return Result.fail("请求异常", CodeEnum.INTERNAL_SERVER_ERROR);
    }
}
