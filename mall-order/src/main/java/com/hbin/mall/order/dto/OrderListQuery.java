package com.hbin.mall.order.dto;

import lombok.Data;

@Data
public class OrderListQuery {
    private Integer status; // 订单状态（可选）
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}