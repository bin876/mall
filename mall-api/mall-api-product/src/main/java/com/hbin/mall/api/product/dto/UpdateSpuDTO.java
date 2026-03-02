package com.hbin.mall.api.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSpuDTO {
    @NotNull(message = "商品ID不能为空")
    @Min(value = 1, message = "商品ID必须大于0")
    private Long spuId;
    
    @NotBlank(message = "商品名称不能为空")
    @Size(min = 2, max = 100, message = "商品名称长度必须在2-100个字符之间")
    private String name;
    
    @NotBlank(message = "商品描述不能为空")
    @Size(max = 500, message = "商品描述长度不能超过500个字符")
    private String description;
    
    @NotNull(message = "商品状态不能为空")
    private Integer publishStatus;
}