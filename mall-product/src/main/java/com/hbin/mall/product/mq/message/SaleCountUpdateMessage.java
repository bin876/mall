package com.hbin.mall.product.mq.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class SaleCountUpdateMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Long spuId;
    private Integer quantity;
}