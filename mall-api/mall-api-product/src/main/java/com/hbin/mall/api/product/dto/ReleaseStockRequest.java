package com.hbin.mall.api.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReleaseStockRequest {
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    @NotNull(message = "释放数量不能为空")
    @Min(value = 1, message = "释放数量必须大于0")
    private Integer quantity;

    private String orderSn;
}