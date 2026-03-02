package com.hbin.mall.api.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateBrandDTO {
    @NotBlank
    private String name;
    @NotEmpty
    private List<Long> categoryIds;
    @NotBlank
    private String logoUrl;
}
