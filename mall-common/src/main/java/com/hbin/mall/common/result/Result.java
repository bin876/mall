package com.hbin.mall.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;

    private Result() {}

    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(
                ResultCode.SUCCESS.getCode(),
                ResultCode.SUCCESS.getMsg(),
                null
        );
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(
                ResultCode.SUCCESS.getCode(),
                ResultCode.SUCCESS.getMsg(),
                data
        );
    }

    public static <T> Result<T> success(T data, String msg) {
        return new Result<>(
                ResultCode.SUCCESS.getCode(),
                msg,
                data
        );
    }

    public static <T> Result<T> error(Integer code) {
        return error(ResultCode.INTERNAL_ERROR);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(
                ResultCode.INTERNAL_ERROR.getCode(),
                msg,
                null
        );
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(
                resultCode.getCode(),
                resultCode.getMsg(),
                null
        );
    }

    public static <T> Result<T> error(ResultCode resultCode, String msg) {
        return new Result<>(
                resultCode.getCode(),
                msg,
                null
        );
    }

    public static <T> Result<T> error(ResultCode resultCode, T data) {
        return new Result<>(
                resultCode.getCode(),
                resultCode.getMsg(),
                data
        );
    }

    public static <T> Result<T> error(int code, String msg, T data) {
        return new Result<>(code, msg, data);
    }
}