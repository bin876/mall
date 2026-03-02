package com.hbin.mall.api.product.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryDto {
    private Long categoryId;
    private Long parentId;
    private String name;
    private Integer level;
    private String icon;
    private Integer status;
    private String parentName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}