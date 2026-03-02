package com.hbin.mall.order.enums;

public enum PayTypeEnum {
    ALIPAY(1, "支付宝"),
    WECHAT(2, "微信"),
    BANK_CARD(3, "银行卡");

    private final int code;
    private final String desc;

    PayTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(Integer code) {
        if (code == null) return "未支付";
        for (PayTypeEnum type : values()) {
            if (type.code == code) {
                return type.desc;
            }
        }
        return "未知支付方式";
    }
}