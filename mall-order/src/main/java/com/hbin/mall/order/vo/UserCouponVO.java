package com.hbin.mall.order.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserCouponVO {
    private Long couponId;
    private Long templateId;
    private String couponCode;
    private Integer status;
    private LocalDateTime receiveTime;
    private LocalDateTime useTime;
    private String orderSn;
    private LocalDateTime expireTime;
    private CouponTemplateVO template;
}