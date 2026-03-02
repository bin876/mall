package com.hbin.mall.api.member.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateMemberDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 64, message = "用户名长度必须在3-64个字符之间")
    private String username;
    
    @Size(max = 64, message = "昵称长度不能超过64个字符")
    private String nickname;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;
    
    @NotNull(message = "性别不能为空")
    private Integer gender;
}