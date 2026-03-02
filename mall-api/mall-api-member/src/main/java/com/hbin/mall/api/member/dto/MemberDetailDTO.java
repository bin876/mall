package com.hbin.mall.api.member.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemberDetailDTO {
    private Long memberId;
    private String username;
    private String phone;
    private String email;
    private String nickname;
    private String avatarUrl;
    private Integer gender;
    private String genderDesc;
    private Integer status;
    private String statusDesc;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}