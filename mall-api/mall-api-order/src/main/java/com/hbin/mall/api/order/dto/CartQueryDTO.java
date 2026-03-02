package com.hbin.mall.api.order.dto;

import lombok.Data;

@Data
public class CartQueryDTO {
    private Long memberId;
    private String skuName;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}