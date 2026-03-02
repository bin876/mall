package com.hbin.mall.api.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserCouponDTO {
    private Long couponId;
    private Long templateId;
    private Long memberId;
    private String memberName;
    private String memberPhone;
    private String couponCode;
    private String templateName;
    private Integer couponType;
    private String couponTypeName;
    private BigDecimal discountAmount;
    private BigDecimal minOrderAmount;
    private Integer status;
    private String statusName;
    private LocalDateTime receiveTime;
    private LocalDateTime useTime;
    private String orderSn;
    private LocalDateTime expireTime;
}