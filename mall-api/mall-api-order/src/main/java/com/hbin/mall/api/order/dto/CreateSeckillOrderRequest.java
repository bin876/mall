package com.hbin.mall.api.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateSeckillOrderRequest {
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotNull(message = "秒杀价格不能为空")
    private BigDecimal seckillPrice;
}