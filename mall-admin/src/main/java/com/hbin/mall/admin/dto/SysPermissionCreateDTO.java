package com.hbin.mall.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SysPermissionCreateDTO {
    @NotBlank(message = "权限标识不能为空")
    @Size(max = 64, message = "权限标识长度不能超过64")
    private String permissionCode;

    @NotBlank(message = "权限名称不能为空")
    @Size(max = 64, message = "权限名称长度不能超过64")
    private String permissionName;
}