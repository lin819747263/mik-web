package com.mik.exception;

import lombok.Getter;

@Getter
public enum CommonErrorCode implements ErrorCode {
    UnKnowException(999, "未知错误,请联系管理员"),
    TooManyRequests(409, "Too Many Requests");

    private Integer code;
    private String message;

    CommonErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
