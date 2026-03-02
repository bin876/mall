package com.hbin.mall.admin.dto;

import lombok.Data;

@Data
public class PermissionQueryDTO {
    private String permissionCode;
    private String permissionName;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}