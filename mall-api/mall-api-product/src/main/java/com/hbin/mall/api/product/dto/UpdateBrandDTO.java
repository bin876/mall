package com.hbin.mall.api.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateBrandDTO {
    @NotNull
    private Long id;
    @NotBlank
    private String name;
    @NotEmpty
    private List<Long> categoryIds;
    private String logoUrl;
}