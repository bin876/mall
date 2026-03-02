package com.hbin.mall.api.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CouponUpdateStatusRequest {
    @NotNull(message = "优惠券ID不能为空")
    private Long couponId;
    
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态不合法")
    @Max(value = 2, message = "状态不合法")
    private Integer status; // 0-未使用, 1-已使用, 2-已过期
    
    private String orderSn; // 使用订单号
}