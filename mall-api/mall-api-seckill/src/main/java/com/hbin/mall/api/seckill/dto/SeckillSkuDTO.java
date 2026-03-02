package com.hbin.mall.api.seckill.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SeckillSkuDTO {
    private Long id;
    private Long activityId;
    private Long spuId;
    private Long skuId;
    private String spuName;
    private String skuName;
    private String defaultImg;
    private BigDecimal price;
    private BigDecimal seckillPrice;
    private Integer totalStock;
    private Integer availableStock;
    
    // 用于前端展示的销售属性
    private List<SaleAttrDTO> saleAttrs;
    
    @Data
    public static class SaleAttrDTO {
        private Long attrId;
        private String attrName;
        private String attrValue;
    }
}