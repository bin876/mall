package com.hbin.mall.common.exception;

/**
 * 业务错误码枚举
 * 所有业务相关的错误码应在此定义
 */
public enum ErrorCode {
    
    // 通用错误码
    SUCCESS(200, "操作成功"),
    BIZ_ERROR(400, "业务错误"),
    VALIDATION_ERROR(4001, "参数校验失败"),
    NOT_FOUND(404, "资源不存在"),
    FORBIDDEN(403, "没有权限"),
    UNAUTHORIZED(401, "未授权"),
    
    // 用户相关
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_ALREADY_EXISTS(1003, "用户已存在"),
    USER_STATUS_INVALID(1004, "用户状态无效"),
    
    // 商品相关
    PRODUCT_NOT_FOUND(2001, "商品不存在"),
    PRODUCT_STOCK_NOT_ENOUGH(2002, "商品库存不足"),
    PRODUCT_STATUS_INVALID(2003, "商品状态无效"),
    
    // 订单相关
    ORDER_NOT_FOUND(3001, "订单不存在"),
    ORDER_STATUS_INVALID(3002, "订单状态无效"),
    ORDER_AMOUNT_ERROR(3003, "订单金额错误"),
    
    // 优惠券相关
    COUPON_NOT_FOUND(4001, "优惠券不存在"),
    COUPON_TEMPLATE_NOT_FOUND(4002, "优惠券模板不存在"),
    COUPON_ALREADY_RECEIVED(4003, "优惠券已领取"),
    COUPON_NOT_AVAILABLE(4004, "优惠券不可用"),
    COUPON_EXPIRED(4005, "优惠券已过期"),
    COUPON_TEMPLATE_DISABLED(4006, "优惠券模板已禁用"),
    COUPON_COUNT_EXCEEDED(4007, "超过每人领取限制"),
    COUPON_OUT_OF_STOCK(4008, "优惠券已发完"),
    
    // 秒杀相关
    SECKILL_NOT_START(5001, "秒杀未开始"),
    SECKILL_ALREADY_END(5002, "秒杀已结束"),
    SECKILL_STOCK_NOT_ENOUGH(5003, "秒杀库存不足"),
    SECKILL_ALREADY_PARTICIPATED(5004, "已参与过本次秒杀"),
    
    // 支付相关
    PAYMENT_FAILED(6001, "支付失败"),
    PAYMENT_ALREADY_PROCESSED(6002, "支付已处理"),
    
    // 系统相关
    SYSTEM_ERROR(5000, "系统错误"),
    SERVICE_UNAVAILABLE(5003, "服务不可用"),
    DATABASE_ERROR(5004, "数据库错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据错误码获取枚举
     * @param code 错误码
     * @return 对应的枚举，如果找不到则返回SYSTEM_ERROR
     */
    public static ErrorCode getByCode(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return SYSTEM_ERROR;
    }
}