package com.hbin.mall.seckill.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillOrderMessage {
    private Long skuId;
    private Long userId;
    private BigDecimal seckillPrice;
    private LocalDateTime createTime;
}