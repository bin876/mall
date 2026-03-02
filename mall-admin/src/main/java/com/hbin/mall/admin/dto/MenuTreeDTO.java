package com.hbin.mall.admin.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuTreeDTO {
    private Long menuId;
    private Long parentId;
    private String title;
    private String name;
    private String path;
    private String redirect;
    private String component;
    private String icon;
    private Integer type;
    private Integer hidden;
    private Integer sort;
    private List<MenuTreeDTO> children = new ArrayList<>();
}