package com.hbin.mall.api.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCategoryDTO {
    @NotBlank
    private String name;
    private String icon;
}