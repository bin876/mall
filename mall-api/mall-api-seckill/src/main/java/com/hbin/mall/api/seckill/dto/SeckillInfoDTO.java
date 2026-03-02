package com.hbin.mall.api.seckill.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
    
    private Integer status; // 0-未开始,1-进行中,2-已结束
}