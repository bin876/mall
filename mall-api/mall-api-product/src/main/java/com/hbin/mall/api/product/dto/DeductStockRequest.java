package com.hbin.mall.api.product.dto;

import lombok.Data;

@Data
public class DeductStockRequest {
    private Long skuId;
    private Integer quantity;
}