package com.hbin.mall.api.product.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SpuDTO {
    private Long spuId;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private Integer publishStatus; // 0-下架, 1-上架
    private Integer saleCount;
    private String defaultImg;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}