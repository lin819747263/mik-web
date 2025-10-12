package com.mik.mvc.exception;

import com.mik.core.constant.ResultCode;
import com.mik.core.exception.ServiceException;
import com.mik.core.pojo.Result;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FeignException.class)
    public Object feignExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        log.error("业务异常", ex);
        FeignException exception = (FeignException)ex;
        return Result.error(1, exception.getMessage());
    }

    @ExceptionHandler(ServiceException.class)
    public Object serviceExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        log.error("业务异常", ex);
        ServiceException exception = (ServiceException)ex;
        return Result.error(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Object undeclaredThrowableExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        log.error("未处理异常", ex);
        return Result.error(ResultCode.UNKNOW.getCode(), ex.getMessage());
    }

}
