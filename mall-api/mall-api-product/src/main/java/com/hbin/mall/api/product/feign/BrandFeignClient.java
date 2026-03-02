package com.hbin.mall.api.product.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.product.dto.*;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "mall-product", contextId = "brand")
public interface BrandFeignClient {

    @PostMapping("/inner/brand/list")
    Result<Page<BrandDTO>> getBrandList(@Valid @RequestBody BrandQueryDTO query);

    @PostMapping("/inner/brand")
    Result<String> createBrand(@Valid @RequestBody BrandCreateDto dto);

    @PutMapping("/inner/brand")
    Result<String> updateBrand(@Valid @RequestBody BrandUpdateDto dto);

    @DeleteMapping("/inner/brand/{id}")
    Result<String> deleteBrand(@NotNull @PathVariable Long id);
}
