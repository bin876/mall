package com.hbin.mall.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysPermissionDto {
    private Long permissionId;
    private String permissionCode;
    private String permissionName;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    private Long usedByRoles = 0L;
    private Long usedByMenus = 0L;
    private Boolean canDelete = true;
}