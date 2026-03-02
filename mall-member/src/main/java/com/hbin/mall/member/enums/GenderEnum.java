package com.hbin.mall.member.enums;

public enum GenderEnum {
    UNKNOWN(0, "未知"),
    MALE(1, "男"),
    FEMALE(2, "女");

    private final int code;
    private final String desc;

    GenderEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(Integer code) {
        if (code == null) return "未知";
        for (GenderEnum gender : values()) {
            if (gender.code == code) {
                return gender.desc;
            }
        }
        return "未知";
    }
}