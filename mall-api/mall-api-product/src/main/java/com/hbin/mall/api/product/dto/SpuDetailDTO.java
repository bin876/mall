package com.hbin.mall.api.product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SpuDetailDTO {
    
    private Spu spu;
    private List<ImageDTO> spuImages;
    private List<AttrValueDTO> spuAttrs;
    private List<SkuDTO> skus;
    
    @Data
    public static class Spu {
        private Long spuId;
        private String name;
        private String description;
        private Long categoryId;
        private Long brandId;
        private BigDecimal weight;
        private Integer publishStatus;
        private String detail;
        private String defaultImg;
    }
    
    @Data
    public static class ImageDTO {
        private String url;
        private Integer sort;
    }
    
    @Data
    public static class AttrValueDTO {
        private Long attrId;
        private String attrName;
        private String attrValue;
    }
    
    @Data
    public static class SkuDTO {
        private Long skuId;
        private String name;
        private String title;
        private String subtitle;
        private BigDecimal price;
        private Integer stock;
        private String defaultImg;
        private List<AttrValueDTO> attrs;
        private List<ImageDTO> images;
    }
}