package com.hbin.mall.api.product.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BatchUpdateCategoryStatusDto {
    @NotNull(message = "分类ID列表不能为空")
    @Size(min = 1, message = "至少需要选择一个分类")
    private List<Long> ids;
    
    @NotNull(message = "状态不能为空")
    private Integer status;
}