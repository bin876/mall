package com.hbin.mall.api.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BrandCreateDto {
    @NotBlank(message = "品牌名称不能为空")
    @Size(min = 2, max = 64, message = "品牌名称长度必须在2-64个字符之间")
    private String name;
    
    @NotBlank(message = "品牌Logo不能为空")
    @Size(max = 500, message = "Logo URL长度不能超过500个字符")
    private String logo;
    
    @NotNull(message = "关联分类不能为空")
    private List<Long> categoryIds;
}