package com.hbin.mall.product.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductDetailDTO {
    private Long spuId;
    private String name;
    private String detail;
    private String defaultImg;
    private List<SpuImageDTO> spuImages;
    private List<SkuDetailDTO> skus;
}