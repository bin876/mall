package com.hbin.mall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.file.feign.FileFeignClient;
import com.hbin.mall.api.product.dto.*;
import com.hbin.mall.api.product.feign.CategoryFeignClient;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
public class CategoryController {

    @Autowired
    private CategoryFeignClient categoryFeignClient;

    @Autowired
    private FileFeignClient fileFeignClient;

    @PostMapping("/list")
    public Result<Page<CategoryDto>> getCategoryList(@Valid @RequestBody CategoryQueryDTO query) {
        return categoryFeignClient.getCategoryList(query);
    }

    @GetMapping("/tree")
    public Result<List<CategoryTreeDto>> getCategoryTree() {
        return categoryFeignClient.getCategoryTree();
    }

    @PostMapping
    public Result<String> createCategory(@Valid @RequestBody CategoryCreateDto dto) {
        return categoryFeignClient.createCategory(dto);
    }

    @PutMapping
    public Result<String> updateCategory(@Valid @RequestBody CategoryUpdateDto dto) {
        return categoryFeignClient.updateCategory(dto);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteCategory(@NotNull @PathVariable Long id) {
        return categoryFeignClient.deleteCategory(id);
    }

    @PutMapping("/batch-status")
    public Result<String> batchUpdateCategoryStatus(@Valid @RequestBody BatchUpdateCategoryStatusDto dto) {
        return categoryFeignClient.batchUpdateCategoryStatus(dto);
    }

    @PostMapping("/icon")
    public Result<String> uploadCategoryIcon(@RequestPart MultipartFile file) {
        return fileFeignClient.upload(file, "category");
    }
}
