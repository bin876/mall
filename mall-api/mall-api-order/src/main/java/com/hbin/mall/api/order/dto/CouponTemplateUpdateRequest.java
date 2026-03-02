package com.hbin.mall.api.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CouponTemplateUpdateRequest extends CouponTemplateCreateRequest {
    @NotNull(message = "模板ID不能为空")
    private Long templateId;
}
