package com.hbin.mall.api.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PublishSpuRequest {
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private Long categoryId;
    @NotNull private Long brandId;
    @NotNull private BigDecimal weight;
    private String detail;
    
    private List<AttrValueDTO> basicAttrs;
    private List<ImageDTO> spuImages;
    private List<PublishSkuDTO> skus;
}