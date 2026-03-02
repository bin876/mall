package com.hbin.mall.api.product.dto;

import lombok.Data;

@Data
public class BannerFormDTO {
    private String title;
    private String imageUrl;
    private String targetUrl;
    private Integer sort;
    private Integer status;
}