package com.hbin.mall.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResourceUpdateDTO {
    @NotNull(message = "资源ID不能为空")
    @Min(value = 1, message = "资源ID必须大于0")
    private Long resourceId;
    
    @NotBlank(message = "资源名称不能为空")
    @Size(max = 64, message = "资源名称长度不能超过64个字符")
    private String name;
    
    @NotBlank(message = "API路径不能为空")
    @Size(max = 200, message = "API路径长度不能超过200个字符")
    private String path;
    
    @NotBlank(message = "请求方法不能为空")
    private String method;
    
    @NotBlank(message = "权限标识不能为空")
    @Size(max = 64, message = "权限标识长度不能超过64个字符")
    private String permissionCode;
    
    @NotNull(message = "状态不能为空")
    private Integer status;
}