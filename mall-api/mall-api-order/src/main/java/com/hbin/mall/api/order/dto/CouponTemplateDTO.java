package com.hbin.mall.api.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponTemplateDTO {
    private Long templateId;
    private String name;
    private String description;
    private Integer couponType;
    private String couponTypeName;
    private BigDecimal discountAmount;
    private BigDecimal minOrderAmount;
    private Integer totalCount;
    private Integer issuedCount; // 已发放数量
    private Integer perUserLimit;
    private Integer validDays;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
}