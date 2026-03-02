package com.hbin.mall.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysUserDto {
    private Long userId;
    private String username;
    private String realName;
    private String nickname;
    private String email;
    private String phone;
    private Integer gender;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}