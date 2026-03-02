package com.hbin.mall.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductListQuery {
    private Long categoryId;
    private Long brandId;
    private String keyword;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sort;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}