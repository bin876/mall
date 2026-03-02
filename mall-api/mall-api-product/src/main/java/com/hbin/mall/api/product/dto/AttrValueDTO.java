package com.hbin.mall.api.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttrValueDTO {
    @NotNull
    private Long attrId;
    private String attrValue;
}