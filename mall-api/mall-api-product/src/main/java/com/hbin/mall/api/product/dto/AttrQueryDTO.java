package com.hbin.mall.api.product.dto;

import lombok.Data;

@Data
public class AttrQueryDTO {
    private String attrName;
    private Integer attrType;
    private Long categoryId;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}