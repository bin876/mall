package com.hbin.mall.api.product.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryTreeDto {
    private Long categoryId;
    private Long parentId;
    private String name;
    private Integer level;
    private String icon;
    private Integer status;
    private List<CategoryTreeDto> children = new ArrayList<>();
}