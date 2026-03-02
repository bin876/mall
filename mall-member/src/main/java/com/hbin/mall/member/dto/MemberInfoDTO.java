package com.hbin.mall.member.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemberInfoDTO {
    private Long memberId;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatarUrl;
    private Integer gender; // 0-未知, 1-男, 2-女
    private LocalDateTime createTime;
}