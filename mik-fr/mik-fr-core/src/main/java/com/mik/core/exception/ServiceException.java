package com.mik.core.exception;

import com.mik.exception.ErrorCode;

public class ServiceException extends RuntimeException{

    private Integer errorCode;

    public ServiceException(ErrorCode error) {
        super(error.getMessage());
        this.errorCode = error.getCode();
    }

    public ServiceException(String errorMessage) {
        super(errorMessage);
        this.errorCode = 1;
    }

    public ServiceException(Integer errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
