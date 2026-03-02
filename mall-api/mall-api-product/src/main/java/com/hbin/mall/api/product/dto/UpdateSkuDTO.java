package com.hbin.mall.api.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateSkuDTO {
    @NotBlank(message = "SKU名称不能为空")
    @Size(min = 2, max = 100, message = "SKU名称长度必须在2-100个字符之间")
    private String name;
    
    @Size(max = 255, message = "标题长度不能超过255个字符")
    private String title;
    
    @Size(max = 255, message = "副标题长度不能超过255个字符")
    private String subtitle;
    
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格不能小于0.01")
    private BigDecimal price;
    
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能小于0")
    private Integer stock;
    
    @NotNull(message = "状态不能为空")
    private Integer status;
}