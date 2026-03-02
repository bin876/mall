package com.hbin.mall.api.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusDTO {
    @NotNull(message = "状态不能为空")
    private Integer status;
}