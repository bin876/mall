package com.hbin.mall.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddCartDTO {
    @NotNull
    private Long skuId;
    
    @Min(value = 1)
    private Integer quantity;
}