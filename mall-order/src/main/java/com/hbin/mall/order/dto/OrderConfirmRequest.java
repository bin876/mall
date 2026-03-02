package com.hbin.mall.order.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderConfirmRequest {
    private List<Long> cartIds;
    private Long skuId;
    private Integer quantity;
}