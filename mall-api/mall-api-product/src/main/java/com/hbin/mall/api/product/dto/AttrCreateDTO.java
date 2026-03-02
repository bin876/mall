package com.hbin.mall.api.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttrCreateDTO {
    @NotBlank(message = "属性名称不能为空")
    private String attrName;
    
    @NotNull(message = "属性类型不能为空")
    private Integer attrType; // 0-销售, 1-基本
    
    @NotNull(message = "值类型不能为空")
    private Integer valueType; // 0-单选, 1-多选
    
    private String valueList; // 逗号分隔（如 "黑色,白色"）
    
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
}
