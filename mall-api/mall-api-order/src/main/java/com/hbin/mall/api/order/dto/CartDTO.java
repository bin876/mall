package com.hbin.mall.api.order.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CartDTO {
    private Long cartId;
    private Long memberId;
    private String memberUsername;
    private Long skuId;
    private String skuName;
    private java.math.BigDecimal skuPrice;
    private String skuPic;
    private Integer quantity;
    private Integer checked; // 0-未选中, 1-选中
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}