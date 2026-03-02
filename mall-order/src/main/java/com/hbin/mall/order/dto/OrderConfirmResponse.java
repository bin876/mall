package com.hbin.mall.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderConfirmResponse {
    private List<OrderSkuDTO> skus;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private String receiverName = "张三";
    private String receiverPhone = "13800138000";
    private String receiverProvince = "广东省";
    private String receiverCity = "深圳市";
    private String receiverRegion = "南山区";
    private String receiverDetailAddress = "科技园南区1号";
}