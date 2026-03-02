package com.hbin.mall.product.dto;

import lombok.Data;

@Data
public class CommentQueryDTO {
    private Long spuId;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}