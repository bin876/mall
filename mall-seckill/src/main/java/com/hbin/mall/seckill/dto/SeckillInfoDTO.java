package com.hbin.mall.seckill.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillInfoDTO {
    private Long activityId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer totalStock;
    private Integer availableStock;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status; // 0-未开始, 1-进行中, 2-已结束
}