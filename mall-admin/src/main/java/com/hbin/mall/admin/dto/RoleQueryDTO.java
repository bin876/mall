package com.hbin.mall.admin.dto;

import lombok.Data;

@Data
public class RoleQueryDTO {
    private String roleCode;
    private String roleName;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}