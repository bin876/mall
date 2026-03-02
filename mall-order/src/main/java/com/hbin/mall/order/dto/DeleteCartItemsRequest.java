package com.hbin.mall.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class DeleteCartItemsRequest {
    @NotNull(message = "购物车ID列表不能为空")
    private List<Long> cartIds;
}