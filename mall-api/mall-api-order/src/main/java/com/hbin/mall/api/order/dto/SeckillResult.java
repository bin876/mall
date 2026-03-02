package com.hbin.mall.api.order.dto;

import lombok.Data;

@Data
public class SeckillResult {
    private boolean success;
    private String orderSn;
    private String payUrl;
}