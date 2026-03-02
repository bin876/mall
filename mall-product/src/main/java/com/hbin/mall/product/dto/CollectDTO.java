package com.hbin.mall.product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CollectDTO {
    private Long collectId;
    private Long spuId;
    private String spuName;
    private String spuPic;
    private BigDecimal spuPrice;
    private LocalDateTime createTime;
    private Boolean collected;
}