package com.hbin.mall.admin.dto;

import lombok.Data;

@Data
public class SysUserInfoDTO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String email;
}