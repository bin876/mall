package com.hbin.mall.order.enums;

public enum PaymentStatusEnum {
    PENDING(0, "待支付"),
    SUCCESS(1, "成功"),
    FAILED(2, "失败"),
    REFUNDED(3, "已退款");

    private final int code;
    private final String desc;

    PaymentStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // ✅ 添加公共 getter（与 OrderStatusEnum 一致）
    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    // ✅ 修正工具方法（使用 getCode()）
    public static String getDescByCode(Integer code) {
        if (code == null) return "未知";
        for (PaymentStatusEnum status : values()) {
            if (status.getCode() == code) { // ← 使用 getCode()
                return status.getDesc();   // ← 使用 getDesc()
            }
        }
        return "未知";
    }
}