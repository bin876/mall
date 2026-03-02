package com.hbin.mall.api.product.dto;

import lombok.Data;

@Data
public class AttrDto {
    private Long attrId;
    private String attrName;
    private Integer attrType;
    private Integer valueType;
    private String valueList;
    private Long categoryId;
}
