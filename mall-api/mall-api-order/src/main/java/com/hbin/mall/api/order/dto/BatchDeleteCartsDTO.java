package com.hbin.mall.api.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BatchDeleteCartsDTO {
    @NotNull(message = "购物车ID列表不能为空")
    @Size(min = 1, message = "至少需要选择一个购物车项")
    private List<Long> cartIds;
}