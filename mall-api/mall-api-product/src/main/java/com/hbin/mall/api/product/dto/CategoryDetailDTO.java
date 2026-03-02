package com.hbin.mall.api.product.dto;

import lombok.Data;

@Data
public class CategoryDetailDTO {
    private Long categoryId;
    private String name;
    private Long parentId;
    private Integer level;
    private String icon;
}