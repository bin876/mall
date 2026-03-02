package com.hbin.mall.product.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.api.product.dto.*;
import com.hbin.mall.common.exception.BusinessException;
import com.hbin.mall.product.domain.Category;
import com.hbin.mall.product.domain.Sku;
import com.hbin.mall.product.domain.Spu;
import com.hbin.mall.product.mapper.CategoryMapper;
import com.hbin.mall.product.mapper.SkuMapper;
import com.hbin.mall.product.mapper.SpuMapper;
import com.hbin.mall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private SpuMapper spuMapper;
    
    @Autowired
    private SkuMapper skuMapper;

    @Override
    public Page<CategoryDto> getCategoryList(CategoryQueryDTO query) {
        Page<Category> page = new Page<>(query.getPageNum(), query.getPageSize());

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Category> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getName())) {
            wrapper.like(Category::getName, query.getName());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Category::getStatus, query.getStatus());
        }

        wrapper.orderByAsc(Category::getLevel)
                .orderByAsc(Category::getCategoryId);

        IPage<Category> categoryPage = categoryMapper.selectPage(page, wrapper);

        List<CategoryDto> categoryDtos = categoryPage.getRecords().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        Map<Long, String> categoryNameMap = categoryPage.getRecords().stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName));

        for (CategoryDto dto : categoryDtos) {
            if (dto.getParentId() > 0) {
                dto.setParentName(categoryNameMap.get(dto.getParentId()));
            } else {
                dto.setParentName("顶级分类");
            }
        }

        return (Page<CategoryDto>) page.convert(record -> {
            for (CategoryDto dto : categoryDtos) {
                if (dto.getCategoryId().equals(record.getCategoryId())) {
                    return dto;
                }
            }
            return convertToDto(record);
        });
    }

    @Override
    public List<CategoryTreeDto> getCategoryTree() {
        List<Category> allCategories = categoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>()
        );

        if (allCategories.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Category> categoryMap = allCategories.stream()
                .collect(Collectors.toMap(Category::getCategoryId, c -> c));

        Map<Long, CategoryTreeDto> dtoMap = allCategories.stream()
                .collect(Collectors.toMap(
                        Category::getCategoryId,
                        this::convertToTreeDto
                ));

        List<CategoryTreeDto> roots = new ArrayList<>();

        for (CategoryTreeDto node : dtoMap.values()) {
            CategoryTreeDto parent = dtoMap.get(node.getParentId());
            if (parent != null) {
                parent.getChildren().add(node);
            } else if (node.getParentId() == 0) {
                roots.add(node);
            }
        }

        return roots;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCategory(CategoryCreateDto dto) {
        boolean nameExists = this.lambdaQuery()
                .eq(Category::getName, dto.getName())
                .eq(Category::getParentId, dto.getParentId())
                .exists();

        if (nameExists) {
            throw new BusinessException("同级分类名称已存在");
        }

        Integer parentLevel = 0;
        if (dto.getParentId() > 0) {
            Category parent = this.getById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException("父分类不存在");
            }
            parentLevel = parent.getLevel();

            if (parentLevel >= 2) {
                throw new BusinessException("分类层级不能超过三级");
            }
        }

        Category category = new Category();
        category.setParentId(dto.getParentId());
        category.setName(dto.getName());
        category.setLevel(parentLevel + 1);
        category.setIcon(dto.getIcon());
        category.setStatus(dto.getStatus());

        this.save(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(CategoryUpdateDto dto) {
        Category existing = this.getById(dto.getCategoryId());
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }

        boolean nameExists = this.lambdaQuery()
                .eq(Category::getName, dto.getName())
                .eq(Category::getParentId, dto.getParentId())
                .ne(Category::getCategoryId, dto.getCategoryId())
                .exists();

        if (nameExists) {
            throw new BusinessException("同级分类名称已存在");
        }

        if (dto.getParentId() > 0) {
            Category parent = this.getById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException("父分类不存在");
            }

            if (parent.getLevel() + 1 != existing.getLevel()) {
                throw new BusinessException("不能改变分类层级");
            }
        }

        existing.setParentId(dto.getParentId());
        existing.setName(dto.getName());
        existing.setIcon(dto.getIcon());
        existing.setStatus(dto.getStatus());

        this.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long categoryId) {
        Category category = this.getById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        boolean hasChildren = this.lambdaQuery()
                .eq(Category::getParentId, categoryId)
                .exists();

        if (hasChildren) {
            throw new BusinessException("分类包含子分类，无法删除");
        }

        boolean hasSpu = spuMapper.exists(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Spu>()
                        .eq(Spu::getCategoryId, categoryId)
        );

        if (hasSpu) {
            throw new BusinessException("分类下存在商品，无法删除");
        }

        boolean hasSku = skuMapper.exists(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Sku>()
                        .eq(Sku::getCategoryId, categoryId)
        );

        if (hasSku) {
            throw new BusinessException("分类下存在商品，无法删除");
        }

        this.removeById(categoryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateCategoryStatus(BatchUpdateCategoryStatusDto dto) {
        List<Category> categories = this.listByIds(dto.getIds());
        if (categories.size() != dto.getIds().size()) {
            throw new BusinessException("部分分类不存在");
        }

        this.lambdaUpdate()
                .set(Category::getStatus, dto.getStatus())
                .in(Category::getCategoryId, dto.getIds())
                .update();
    }

    private CategoryDto convertToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setCategoryId(category.getCategoryId());
        dto.setParentId(category.getParentId());
        dto.setName(category.getName());
        dto.setLevel(category.getLevel());
        dto.setIcon(category.getIcon());
        dto.setStatus(category.getStatus());
        dto.setCreateTime(category.getCreateTime());
        dto.setUpdateTime(category.getUpdateTime());
        return dto;
    }

    private CategoryTreeDto convertToTreeDto(Category category) {
        CategoryTreeDto dto = new CategoryTreeDto();
        dto.setCategoryId(category.getCategoryId());
        dto.setParentId(category.getParentId());
        dto.setName(category.getName());
        dto.setLevel(category.getLevel());
        dto.setIcon(category.getIcon());
        dto.setStatus(category.getStatus());
        return dto;
    }
}
