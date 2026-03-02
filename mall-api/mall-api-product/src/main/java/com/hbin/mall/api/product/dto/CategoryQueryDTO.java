package com.hbin.mall.api.product.dto;

import lombok.Data;

@Data
public class CategoryQueryDTO {
    private String name;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}