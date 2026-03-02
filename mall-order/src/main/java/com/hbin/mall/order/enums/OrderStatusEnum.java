package com.hbin.mall.order.enums;

public enum OrderStatusEnum {
    UNPAID(0, "待付款"),
    PAID(1, "已付款"),
    DELIVERED(2, "已发货"),
    COMPLETED(3, "已完成"),
    CLOSED(4, "已关闭"),
    CANCELLED(5, "已取消");

    private final int code;
    private final String desc;

    OrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // ✅ 添加公共 getter
    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    // ✅ 静态工具方法（保持原有逻辑）
    public static String getDescByCode(Integer code) {
        for (OrderStatusEnum status : values()) {
            if (status.getCode() == code) { // ← 使用 getCode()
                return status.getDesc();
            }
        }
        return "未知状态";
    }

    public static boolean canDeliver(Integer status) {
        return PAID.getCode() == status; // ← 使用 getCode()
    }

    public static boolean canClose(Integer status) {
        return UNPAID.getCode() == status || PAID.getCode() == status;
    }

    public static Integer getCodeByDesc(String desc) {
        for (OrderStatusEnum status : values()) {
            if (status.getDesc().equals(desc)) {
                return status.getCode(); // 自动装箱为 Integer
            }
        }
        return null;
    }
}