package com.hbin.mall.api.product.dto;

import lombok.Data;

@Data
public class CategoryListDTO {

    private Long categoryId;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类层级
     */
    private Integer level;

    /**
     * 分类图标
     */
    private String icon;
}
