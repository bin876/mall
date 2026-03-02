package com.hbin.mall.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SysPermissionUpdateDTO {
    @NotNull(message = "权限ID不能为空")
    @Min(value = 1, message = "权限ID必须大于0")
    private Long permissionId;
    
    @NotBlank(message = "权限标识不能为空")
    @Size(max = 64, message = "权限标识长度不能超过64个字符")
    private String permissionCode;
    
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 64, message = "权限名称长度不能超过64个字符")
    private String permissionName;
    
    @NotNull(message = "状态不能为空")
    private Integer status;
}