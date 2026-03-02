package com.hbin.mall.api.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CouponIssueRequest {
    @NotNull(message = "模板ID不能为空")
    private Long templateId;
    
    @NotNull(message = "用户ID列表不能为空")
    @Size(min = 1, message = "至少需要一个用户ID")
    private List<@NotNull Long> memberIds;
    
    private String issueType = "DIRECT"; // DIRECT:直接发放, IMPORT:导入发放
}
