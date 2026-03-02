package com.hbin.mall.api.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttrUpdateDTO {
    @NotNull(message = "属性ID不能为空")
    private Long attrId;
    
    @NotBlank(message = "属性名称不能为空")
    private String attrName;
    
    @NotNull(message = "属性类型不能为空")
    private Integer attrType;
    
    @NotNull(message = "值类型不能为空")
    private Integer valueType;
    
    private String valueList;
    
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
}