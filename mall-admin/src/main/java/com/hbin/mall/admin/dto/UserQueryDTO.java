package com.hbin.mall.admin.dto;

import lombok.Data;

@Data
public class UserQueryDTO {
    private String username;
    private String realName;
    private String phone;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}