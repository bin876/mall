package com.hbin.mall.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.product.dto.*;
import com.hbin.mall.api.product.feign.BrandFeignClient;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.product.service.BrandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdminBrandController implements BrandFeignClient {

    @Autowired
    private BrandService brandService;

    @Override
    public Result<Page<BrandDTO>> getBrandList(@Valid @RequestBody BrandQueryDTO query) {
        Page<BrandDTO> result = brandService.getBrandList(query);
        return Result.success(result);
    }

    @Override
    public Result<String> createBrand(@Valid @RequestBody BrandCreateDto dto) {
        brandService.createBrand(dto);
        return Result.success("品牌创建成功");
    }

    @Override
    public Result<String> updateBrand(@Valid @RequestBody BrandUpdateDto dto) {
        brandService.updateBrand(dto);
        return Result.success("品牌更新成功");
    }

    @Override
    public Result<String> deleteBrand(@NotNull @PathVariable Long id) {
        brandService.deleteBrand(id);
        return Result.success("品牌删除成功");
    }
}