package com.hbin.mall.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或认证失败"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统内部错误"),

    LOGIN_FAILED(1001, "用户名或密码错误"),
    ACCOUNT_DISABLED(1002, "账号已禁用"),
    ACCOUNT_LOCKED(1003, "账号已锁定"),
    TOKEN_EXPIRED(1004, "登录已过期，请重新登录"),
    TOKEN_INVALID(1005, "无效的登录信息"),

    OPERATION_FAILED(2001, "操作失败"),
    DATA_CONFLICT(2002, "数据冲突或已存在"),
    ILLEGAL_REQUEST(2003, "非法请求");

    private final Integer code;
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
