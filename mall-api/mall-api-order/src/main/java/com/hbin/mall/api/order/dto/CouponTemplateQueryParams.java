package com.hbin.mall.api.order.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CouponTemplateQueryParams {
    private String name;
    private Integer couponType;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}