package com.hbin.mall.api.order.dto;

import lombok.Data;

@Data
public class CreateOrderResponse {
    private Long orderId;
    private String orderSn;
}