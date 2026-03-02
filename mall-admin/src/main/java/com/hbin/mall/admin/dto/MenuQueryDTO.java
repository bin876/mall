package com.hbin.mall.admin.dto;

import lombok.Data;

@Data
public class MenuQueryDTO {
    private String title;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}