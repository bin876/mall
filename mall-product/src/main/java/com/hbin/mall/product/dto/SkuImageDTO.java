package com.hbin.mall.product.dto;

import lombok.Data;

@Data
public class SkuImageDTO {
    private String imgUrl;
    private Integer sort;
    private Integer isDefault;
}