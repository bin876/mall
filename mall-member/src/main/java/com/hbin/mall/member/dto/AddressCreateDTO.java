package com.hbin.mall.member.dto;

import lombok.Data;

@Data
public class AddressCreateDTO {
    private String receiverName;
    private String receiverPhone;
    private String receiverProvince;
    private String receiverCity;
    private String receiverRegion;
    private String receiverDetailAddress;
    private String receiverPostCode;
    private Boolean isDefault;
}