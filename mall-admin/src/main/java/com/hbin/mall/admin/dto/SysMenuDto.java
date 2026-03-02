package com.hbin.mall.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class SysMenuDto {
    private Long menuId;
    private Long parentId;
    private String title;
    private String name;
    private String path;
    private String component;
    private String permissionCode;
    private String icon;
    private Integer type; // 0-目录, 1-菜单, 2-按钮
    private Integer sort;
    private Integer hidden; // 0-显示, 1-隐藏
    private Integer status; // 0-禁用, 1-启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<SysMenuDto> children = new ArrayList<>();
}