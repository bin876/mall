package com.hbin.mall.order.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private List<Long> cartIds;
    private Long skuId;
    private Integer quantity;

    private String receiverName;
    private String receiverPhone;
    private String receiverProvince;
    private String receiverCity;
    private String receiverRegion;
    private String receiverDetailAddress;
    private String couponCode;
}