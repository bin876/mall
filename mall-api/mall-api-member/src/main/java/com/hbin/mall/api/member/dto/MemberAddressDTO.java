package com.hbin.mall.api.member.dto;

import lombok.Data;

@Data
public class MemberAddressDTO {
    private Long addressId;
    private Long memberId;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String region;
    private String detailAddress;
    private String postCode;
    private Boolean isDefault;
}