package com.hbin.mall.api.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMemberStatusDTO {
    @NotNull(message = "状态不能为空")
    private Integer status;
}