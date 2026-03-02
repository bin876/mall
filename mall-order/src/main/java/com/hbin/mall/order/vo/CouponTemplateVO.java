package com.hbin.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CouponTemplateVO {
    private Long templateId;
    private String name;
    private String description;
    private Integer couponType;
    private BigDecimal discountAmount;
    private BigDecimal minOrderAmount;
}