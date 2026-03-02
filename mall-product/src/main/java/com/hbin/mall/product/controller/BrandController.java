package com.hbin.mall.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.product.dto.BrandDetailDto;
import com.hbin.mall.api.product.dto.BrandDTO;
import com.hbin.mall.api.product.dto.BrandQueryDTO;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product/brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @GetMapping("/list")
    public Result<Page<BrandDTO>> getBrandList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        BrandQueryDTO query = new BrandQueryDTO();
        query.setName(name);
        query.setCategoryId(categoryId);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);

        Page<BrandDTO> page = brandService.getBrandList(query);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<BrandDetailDto> getBrandById(@PathVariable Long id) {
        BrandDetailDto brand = brandService.getBrandById(id);
        return Result.success(brand);
    }
}
