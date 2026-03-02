package com.hbin.mall.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitPaymentRequest {
    @NotBlank(message = "订单编号不能为空")
    private String orderSn;
    
    @NotNull(message = "支付方式不能为空")
    private Integer payType; // 1-支付宝, 2-微信, 3-银行卡
}