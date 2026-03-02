package com.hbin.mall.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateMemberInfoDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    private String nickname;
    private String avatarUrl;
    private Integer gender; // 0-未知, 1-男, 2-女
}