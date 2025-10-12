package com.mik.core.pojo;

import com.mik.core.constant.ResultCode;
import com.mik.exception.ErrorCode;
import lombok.Data;

@Data
public class Result<T> {
    private Integer code = 0;
    private String msg = "";
    private T data;

    public static final Result DELETE_SUCCESS =  Result.success( "删除成功");
    public static final Result MODIFY_SUCCESS =  Result.success( "修改成功");
    public static final Result CREATE_SUCCESS =  Result.success( "创建成功");

    public Result() {
    }

    private Result(Integer code) {
        this.code = code;
    }

    private Result(Integer code, T data) {
        this.code = code;
        this.data = data;
    }

    private Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static Result<Void> error(String msg) {
        return new Result<>(ResultCode.UNAUTHORTHORIZED.getCode(), msg);
    }

    public static Result<Void> error(Integer code, String msg) {
        return new Result<>(code, msg);
    }

    public static Result<Void> error(ErrorCode code) {
        return new Result<>(code.getCode(), code.getMessage());
    }

    public static Result<Void> success() {
        return new Result<>(ResultCode.SUCCESS.getCode());
    }

    public static Result<Void> success(String msg) {
        return new Result<>(ResultCode.SUCCESS.getCode(), msg);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), data);
    }

}
