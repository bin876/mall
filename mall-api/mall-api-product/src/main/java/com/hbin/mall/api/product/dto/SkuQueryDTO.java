package com.hbin.mall.api.product.dto;

import lombok.Data;

@Data
public class SkuQueryDTO {
    private Long spuId;
    private String keyword;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String spuName;
    private Integer categoryId;
    private Integer brandId;
}