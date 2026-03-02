package com.hbin.mall.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SysMenuUpdateDTO extends SysMenuBaseDTO {
    @NotNull(message = "菜单ID不能为空")
    @Min(value = 1, message = "菜单ID必须大于0")
    private Long menuId;
    
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 100, message = "菜单名称长度不能超过100个字符")
    private String title;
    
    @Size(max = 100, message = "路由名称长度不能超过100个字符")
    private String name;
    
    @Size(max = 200, message = "路由路径长度不能超过200个字符")
    private String path;
    
    @Size(max = 200, message = "组件路径长度不能超过200个字符")
    private String component;
    
    @Size(max = 64, message = "权限标识长度不能超过64个字符")
    private String permissionCode;
    
    @Size(max = 255, message = "菜单图标长度不能超过255个字符")
    private String icon;
    
    @NotNull(message = "菜单类型不能为空")
    private Integer type;
    
    private Integer sort = 0;
    private Integer hidden = 0;
    private Integer status = 1;
}