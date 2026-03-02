package com.hbin.mall.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.product.dto.*;
import com.hbin.mall.api.product.feign.CategoryFeignClient;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.product.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AdminCategoryController implements CategoryFeignClient {

    @Autowired
    private CategoryService categoryService;

    @Override
    public Result<Page<CategoryDto>> getCategoryList(@Valid @RequestBody CategoryQueryDTO query) {
        Page<CategoryDto> result = categoryService.getCategoryList(query);
        return Result.success(result);
    }

    @Override
    public Result<List<CategoryTreeDto>> getCategoryTree() {
        List<CategoryTreeDto> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }

    @Override
    public Result<String> createCategory(@Valid @RequestBody CategoryCreateDto dto) {
        categoryService.createCategory(dto);
        return Result.success("分类创建成功");
    }

    @Override
    public Result<String> updateCategory(@Valid @RequestBody CategoryUpdateDto dto) {
        categoryService.updateCategory(dto);
        return Result.success("分类更新成功");
    }

    @Override
    public Result<String> deleteCategory(@NotNull @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("分类删除成功");
    }

    @Override
    public Result<String> batchUpdateCategoryStatus(@Valid @RequestBody BatchUpdateCategoryStatusDto dto) {
        categoryService.batchUpdateCategoryStatus(dto);
        return Result.success("批量操作成功");
    }
}
