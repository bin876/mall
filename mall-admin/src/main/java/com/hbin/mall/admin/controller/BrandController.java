package com.hbin.mall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.file.feign.FileFeignClient;
import com.hbin.mall.api.product.dto.*;
import com.hbin.mall.api.product.feign.BrandFeignClient;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/brand")
public class BrandController {

    @Autowired
    private BrandFeignClient brandFeignClient;

    @Autowired
    private FileFeignClient fileFeignClient;

    @PostMapping("/list")
    public Result<Page<BrandDTO>> getBrandList(@Valid @RequestBody BrandQueryDTO query) {
        return brandFeignClient.getBrandList(query);
    }

    @PostMapping
    public Result<String> createBrand(@Valid @RequestBody BrandCreateDto dto) {
        return brandFeignClient.createBrand(dto);
    }

    @PutMapping
    public Result<String> updateBrand(@Valid @RequestBody BrandUpdateDto dto) {
        return brandFeignClient.updateBrand(dto);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteBrand(@NotNull @PathVariable Long id) {
        return brandFeignClient.deleteBrand(id);
    }

    @PostMapping("/logo")
    public Result<String> uploadBrandLogo(@RequestPart MultipartFile file) {
        return fileFeignClient.upload(file, "brand");
    }
}