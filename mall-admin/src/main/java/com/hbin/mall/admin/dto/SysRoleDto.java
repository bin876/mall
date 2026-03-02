package com.hbin.mall.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysRoleDto {
    private Long roleId;
    private String roleCode;
    private String roleName;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    private Long usedByUsers = 0L;
    private Boolean canDelete = true;
}