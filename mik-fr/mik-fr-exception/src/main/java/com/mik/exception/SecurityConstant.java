package com.mik.exception;


import lombok.Getter;

@Getter
public enum SecurityConstant implements ErrorCode {
    USERNAME_PASSWORD_ERROR(5001, "用户名或密码错误"),
    LOGIN_LIMIT(5002,"登录限制"),
    AUTH_ERROR(5003,"授权信息过期"),
    NO_PERMISSION(5004,"您无权限操作")
    ;

    private Integer code;
    private String message;

    SecurityConstant(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
