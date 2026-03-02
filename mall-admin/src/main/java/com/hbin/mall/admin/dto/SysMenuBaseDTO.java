package com.hbin.mall.admin.dto;

import lombok.Data;

@Data
public class SysMenuBaseDTO {
    private Long parentId = 0L;
    private String title;
    private String name;
    private String path;
    private String redirect;
    private String component;
    private String permissionCode;
    private String icon;
    private Integer type;
    private Integer sort = 0;
    private Integer hidden = 0;
}