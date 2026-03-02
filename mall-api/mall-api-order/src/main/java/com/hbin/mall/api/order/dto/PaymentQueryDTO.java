package com.hbin.mall.api.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentQueryDTO {
    private String orderSn;
    private String paySn;
    private Integer payType;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
