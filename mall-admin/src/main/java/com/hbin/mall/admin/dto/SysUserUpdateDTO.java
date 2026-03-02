package com.hbin.mall.admin.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class SysUserUpdateDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 64, message = "真实姓名长度不能超过64个字符")
    private String realName;
    
    @Size(max = 64, message = "昵称长度不能超过64个字符")
    private String nickname;
    
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    private Integer gender = 0;
    
    @NotNull(message = "状态不能为空")
    private Integer status;
    
    @NotNull(message = "角色列表不能为空")
    @Size(min = 1, message = "至少需要分配一个角色")
    private List<Long> roleIds;
}