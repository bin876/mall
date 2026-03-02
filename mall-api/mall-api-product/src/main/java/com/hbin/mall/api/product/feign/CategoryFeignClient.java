package com.hbin.mall.api.product.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.product.dto.*;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "mall-product", contextId = "category")
public interface CategoryFeignClient {

    @PostMapping("/inner/category/list")
    Result<Page<CategoryDto>> getCategoryList(@Valid @RequestBody CategoryQueryDTO query);

    @GetMapping("/inner/category/tree")
    Result<List<CategoryTreeDto>> getCategoryTree();

    @PostMapping("/inner/category")
    Result<String> createCategory(@Valid @RequestBody CategoryCreateDto dto);

    @PutMapping("/inner/category")
    Result<String> updateCategory(@Valid @RequestBody CategoryUpdateDto dto);

    @DeleteMapping("/inner/category/{id}")
    Result<String> deleteCategory(@NotNull @PathVariable Long id);

    @PutMapping("/inner/category/batch-status")
    Result<String> batchUpdateCategoryStatus(@Valid @RequestBody BatchUpdateCategoryStatusDto dto);
}
