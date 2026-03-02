package com.hbin.mall.order.dto;

import lombok.Data;

@Data
public class UpdateCartDTO {
    private Long cartId;
    private Integer quantity;
    private Boolean checked;
}