package com.hbin.mall.api.product.dto;

import lombok.Data;

@Data
public class SpuQueryDTO {
    private String name;
    private Long categoryId;
    private Long brandId;
    private Integer publishStatus;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
