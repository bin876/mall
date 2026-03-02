package com.hbin.mall.api.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCategoryDTO {
    @NotBlank
    private String name;
    @NotNull
    private Long parentId;
    @NotNull
    private Integer level;
    private String icon;
    private Integer sort = 0;
}