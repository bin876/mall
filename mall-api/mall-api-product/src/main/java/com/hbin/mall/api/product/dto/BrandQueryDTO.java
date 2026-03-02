package com.hbin.mall.api.product.dto;

import lombok.Data;

@Data
public class BrandQueryDTO {
    private String name;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Long categoryId;
}