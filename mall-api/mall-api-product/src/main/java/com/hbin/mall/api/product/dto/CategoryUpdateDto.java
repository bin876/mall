package com.hbin.mall.api.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryUpdateDto {
    @NotNull(message = "分类ID不能为空")
    @Min(value = 1)
    private Long categoryId;
    
    @Min(value = 0)
    private Long parentId = 0L;
    
    @NotBlank(message = "分类名称不能为空")
    @Size(min = 2, max = 64, message = "分类名称长度必须在2-64个字符之间")
    private String name;
    
    @Size(max = 500, message = "图标URL长度不能超过500个字符")
    private String icon;
    
    @NotNull(message = "状态不能为空")
    private Integer status;
}