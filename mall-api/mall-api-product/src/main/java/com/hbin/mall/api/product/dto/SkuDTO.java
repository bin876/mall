package com.hbin.mall.api.product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SkuDTO {
    private Long skuId;
    private Long spuId;
    private String spuName;
    private String name;
    private String title;
    private String subtitle;
    private BigDecimal price;
    private Integer stock;
    private Integer status; // 0-下架, 1-上架
    private String defaultImg;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<SaleAttrDTO> saleAttrs;
    
    @Data
    public static class SaleAttrDTO {
        private Long attrId;
        private String attrName;
        private String attrValue;
    }
}