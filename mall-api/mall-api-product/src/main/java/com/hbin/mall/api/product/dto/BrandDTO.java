package com.hbin.mall.api.product.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BrandDTO {
    private Long id;
    private String name;
    private String logo;
    private String firstLetter;
    private List<Long> categoryIds;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}