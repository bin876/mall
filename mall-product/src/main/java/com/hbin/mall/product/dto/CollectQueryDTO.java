package com.hbin.mall.product.dto;

import lombok.Data;

@Data
public class CollectQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}