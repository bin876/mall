package com.hbin.mall.member.enums;

public enum MemberStatusEnum {
    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final int code;
    private final String desc;

    MemberStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(Integer code) {
        if (code == null) return "未知";
        for (MemberStatusEnum status : values()) {
            if (status.code == code) {
                return status.desc;
            }
        }
        return "未知";
    }
}