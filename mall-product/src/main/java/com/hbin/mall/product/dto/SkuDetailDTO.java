package com.hbin.mall.product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuDetailDTO {
    private Long skuId;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private List<SkuAttrValueDTO> attrs;
    private List<SkuImageDTO> images;
}