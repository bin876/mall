package com.hbin.mall.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderSimpleDTO {
    private Long orderId;
    private String orderSn;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer status;
    private String statusDesc;
    private LocalDateTime createTime;
    private Integer productCount;
    private String firstProductImg;
}