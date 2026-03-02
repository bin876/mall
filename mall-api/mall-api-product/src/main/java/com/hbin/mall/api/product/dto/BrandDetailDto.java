package com.hbin.mall.api.product.dto;

import lombok.Data;

import java.util.List;

@Data
public class BrandDetailDto {
    private Long id;
    private String name;
    private String logo;
    private String firstLetter;
    private List<Long> categoryIds;
}