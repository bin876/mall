package com.hbin.mall.api.order.dto;

import lombok.Data;

@Data
public class OrderQueryDTO {
    private String orderSn;
    private Long memberId;
    private String username;
    private Integer status;
    private String startTime;
    private String endTime;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
