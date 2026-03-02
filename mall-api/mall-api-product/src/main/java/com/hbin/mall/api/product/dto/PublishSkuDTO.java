package com.hbin.mall.api.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PublishSkuDTO {
    @NotBlank
    private String name;
    private String title;
    private String subtitle;
    @NotNull
    private BigDecimal price;
    @NotNull private Integer stock;
    
    private List<AttrValueDTO> saleAttrs;
    private List<ImageDTO> images;
}