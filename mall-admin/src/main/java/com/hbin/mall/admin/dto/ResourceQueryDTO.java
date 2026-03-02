package com.hbin.mall.admin.dto;

import lombok.Data;

@Data
public class ResourceQueryDTO {
    private String name;
    private String path;
    private String permissionCode;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}