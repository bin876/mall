package com.hbin.mall.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysResourceDto {
    private Long resourceId;
    private String name;
    private String path;
    private String method;
    private String permissionCode;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}