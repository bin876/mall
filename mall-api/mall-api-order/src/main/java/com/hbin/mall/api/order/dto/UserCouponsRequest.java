package com.hbin.mall.api.order.dto;

import lombok.Data;

@Data
public class UserCouponsRequest {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private UserCouponQueryParams params;
}
