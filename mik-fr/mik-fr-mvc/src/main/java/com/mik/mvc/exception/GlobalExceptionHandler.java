package com.mik.mvc.exception;

import com.mik.core.constant.ResultCode;
import com.mik.core.exception.ServiceException;
import com.mik.core.pojo.Result;
import com.mik.exception.CommonErrorCode;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {MethodArgumentNotValidException.class, ConstraintViolationException.class, BindException.class})
    public Object paramExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        log.debug("参数异常", ex);
        return Result.error(CommonErrorCode.paramError);
    }

    @ExceptionHandler(FeignException.class)
    public Object feignExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        log.debug("业务异常", ex);
        FeignException exception = (FeignException)ex;
        return Result.error(1, exception.getMessage());
    }

    @ExceptionHandler(ServiceException.class)
    public Object serviceExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        log.debug("业务异常", ex);
        ServiceException exception = (ServiceException)ex;
        return Result.error(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Object undeclaredThrowableExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        log.debug("未处理异常", ex);
        return Result.error(ResultCode.UNKNOW.getCode(), ex.getMessage());
    }

}
