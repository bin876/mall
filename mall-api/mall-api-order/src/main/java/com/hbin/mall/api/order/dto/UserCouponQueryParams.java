package com.hbin.mall.api.order.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserCouponQueryParams {
    private Long memberId;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
