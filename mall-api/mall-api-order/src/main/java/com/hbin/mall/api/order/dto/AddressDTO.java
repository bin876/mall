package com.hbin.mall.api.order.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private Long addressId;
    private String receiverName;
    private String receiverPhone;
    private String receiverProvince;
    private String receiverCity;
    private String receiverRegion;
    private String receiverDetailAddress;
    private String receiverPostCode;
    private Boolean isDefault;
}
