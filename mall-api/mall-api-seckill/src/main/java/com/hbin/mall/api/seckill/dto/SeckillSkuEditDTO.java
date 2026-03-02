package com.hbin.mall.api.seckill.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 秒杀SKU编辑用DTO
 * 包含编辑所需的额外字段，用于价格验证、库存验证等
 */
@Data
public class SeckillSkuEditDTO {
    
    // 基础字段
    private Long id;
    private Long activityId;
    private Long spuId;
    private Long skuId;
    private String spuName;
    private String skuName;
    private String defaultImg;
    private BigDecimal seckillPrice;
    private Integer totalStock;
    private Integer availableStock;
    
    // 编辑专用字段
    private BigDecimal originalPrice;   // 商品原始价格，用于价格验证
    private Integer maxStock;            // 商品总库存，用于库存验证
    private Integer originalStock;       // 原始库存，用于对比修改
    
    // 商品属性相关
    private List<SaleAttrDTO> saleAttrs; // 销售属性，用于显示规格信息
    
    // 业务规则字段
    private Boolean canEditStock;        // 是否允许编辑库存
    private Boolean canEditPrice;        // 是否允许编辑价格
    private String priceLimitMessage;   // 价格限制提示信息
    private String stockLimitMessage;   // 库存限制提示信息
    
    /**
     * 销售属性DTO
     */
    @Data
    public static class SaleAttrDTO {
        private Long attrId;
        private String attrName;
        private String attrValue;
    }
    
    // 其他辅助方法...
    
    /**
     * 从SkuDTO.SaleAttrDTO转换
     */
    public static List<SaleAttrDTO> fromSkuSaleAttrs(List<com.hbin.mall.api.product.dto.SkuDTO.SaleAttrDTO> skuAttrs) {
        if (skuAttrs == null || skuAttrs.isEmpty()) {
            return Collections.emptyList();
        }
        
        return skuAttrs.stream()
                .map(attr -> {
                    SaleAttrDTO result = new SaleAttrDTO();
                    result.setAttrId(attr.getAttrId());
                    result.setAttrName(attr.getAttrName());
                    result.setAttrValue(attr.getAttrValue());
                    return result;
                })
                .collect(Collectors.toList());
    }
}