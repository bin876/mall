package com.hbin.mall.order.dto;

import lombok.Data;

@Data
public class PaymentResponse {
    private String message = "支付成功";
    private String orderSn;
}