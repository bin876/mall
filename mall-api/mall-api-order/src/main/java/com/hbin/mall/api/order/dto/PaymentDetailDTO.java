package com.hbin.mall.api.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDetailDTO {
    private Long paymentId;
    private Long memberId;
    private Long orderId;
    private String orderSn;
    private String paySn;
    private Integer payType;
    private String payTypeDesc;
    private BigDecimal amount;
    private Integer status;
    private String statusDesc;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String callbackContent;
}