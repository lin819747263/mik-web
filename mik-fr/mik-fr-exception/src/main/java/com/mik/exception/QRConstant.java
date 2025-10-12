package com.mik.exception;


public enum QRConstant implements ErrorCode {
    USER_AUTH_EXIST(6001, "用户已存在"),
    ;

    private Integer code;
    private String message;

    QRConstant(Integer code, String message) {
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
