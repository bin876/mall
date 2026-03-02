package com.hbin.mall.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private Long cartId;
    private Long skuId;
    private String skuName;
    private BigDecimal skuPrice;
    private String skuPic;
    private Integer quantity;
    private Boolean checked;
}