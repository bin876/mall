package com.hbin.mall.product.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SpuSimpleDTO {
    private Long spuId;
    private String name;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer saleCount;
    private String defaultImg;
    private LocalDateTime createTime;
}