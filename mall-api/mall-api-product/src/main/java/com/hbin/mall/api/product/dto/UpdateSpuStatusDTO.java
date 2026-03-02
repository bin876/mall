package com.hbin.mall.api.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSpuStatusDTO {
    @NotNull(message = "状态不能为空")
    private Integer publishStatus;
}