package com.hbin.mall.api.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderDetailDTO {
    // 订单主表字段
    private Long orderId;
    private String orderSn;
    private Long memberId;
    private String username;
    private BigDecimal totalAmount;
    private BigDecimal freightAmount;
    private BigDecimal promotionAmount;
    private BigDecimal payAmount;
    private Integer status;
    private String statusDesc;
    private Integer payType;
    private String payTypeDesc;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime receiveTime;
    private LocalDateTime closeTime;
    
    // 收货信息
    private String receiverName;
    private String receiverPhone;
    private String receiverPostCode;
    private String receiverProvince;
    private String receiverCity;
    private String receiverRegion;
    private String receiverDetailAddress;
    private Integer autoConfirmDay;
    private String note;
    
    // 订单项
    private List<OrderItemDTO> orderItems = new ArrayList<>();
}