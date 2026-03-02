package com.hbin.mall.api.member.dto;

import lombok.Data;

@Data
public class MemberQueryDTO {
    private String username;
    private String nickname;
    private String phone;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
