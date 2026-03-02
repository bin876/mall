package com.hbin.mall.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.api.product.dto.*;
import com.hbin.mall.product.domain.Category;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CategoryService extends IService<Category> {

    Page<CategoryDto> getCategoryList(CategoryQueryDTO query);

    List<CategoryTreeDto> getCategoryTree();

    @Transactional(rollbackFor = Exception.class)
    void createCategory(CategoryCreateDto dto);

    @Transactional(rollbackFor = Exception.class)
    void updateCategory(CategoryUpdateDto dto);

    @Transactional(rollbackFor = Exception.class)
    void deleteCategory(Long categoryId);

    @Transactional(rollbackFor = Exception.class)
    void batchUpdateCategoryStatus(BatchUpdateCategoryStatusDto dto);
}
