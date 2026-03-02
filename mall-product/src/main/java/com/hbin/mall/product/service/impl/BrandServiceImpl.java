package com.hbin.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.api.product.dto.*;
import com.hbin.mall.common.exception.BusinessException;
import com.hbin.mall.product.domain.Brand;
import com.hbin.mall.product.domain.Category;
import com.hbin.mall.product.domain.CategoryBrand;
import com.hbin.mall.product.mapper.BrandMapper;
import com.hbin.mall.product.mapper.CategoryBrandMapper;
import com.hbin.mall.product.mapper.CategoryMapper;
import com.hbin.mall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    @Override
    public Page<BrandDTO> getBrandList(BrandQueryDTO query) {
        Page<Brand> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<Brand> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getName())) {
            wrapper.like(Brand::getName, query.getName());
        }

        if (query.getCategoryId() != null && query.getCategoryId() > 0) {
            return getBrandsByCategory(page, query);
        }

        wrapper.orderByAsc(Brand::getFirstLetter);

        IPage<Brand> brandPage = brandMapper.selectPage(page, wrapper);

        return (Page<BrandDTO>) brandPage.convert(this::convertToDto);
    }

    private Page<BrandDTO> getBrandsByCategory(Page<Brand> page, BrandQueryDTO query) {
        List<Long> brandIds = categoryBrandMapper.selectList(
                        new LambdaQueryWrapper<CategoryBrand>()
                                .eq(CategoryBrand::getCategoryId, query.getCategoryId())
                ).stream()
                .map(CategoryBrand::getBrandId)
                .collect(Collectors.toList());

        if (brandIds.isEmpty()) {
            return new Page<BrandDTO>()
                    .setCurrent(query.getPageNum())
                    .setSize(query.getPageSize())
                    .setTotal(0)
                    .setRecords(Collections.emptyList());
        }

        LambdaQueryWrapper<Brand> brandWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getName())) {
            brandWrapper.like(Brand::getName, query.getName());
        }

        brandWrapper.in(Brand::getId, brandIds)
                .orderByAsc(Brand::getFirstLetter);

        IPage<Brand> brandPage = brandMapper.selectPage(page, brandWrapper);

        return (Page<BrandDTO>) brandPage.convert(this::convertToDto);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBrand(BrandCreateDto dto) {
        boolean nameExists = this.lambdaQuery()
                .eq(Brand::getName, dto.getName())
                .exists();

        if (nameExists) {
            throw new BusinessException("品牌名称已存在");
        }

        List<Category> categories = categoryMapper.selectBatchIds(dto.getCategoryIds());
        if (categories.size() != dto.getCategoryIds().size()) {
            throw new BusinessException("部分分类ID不存在");
        }

        Brand brand = new Brand();
        brand.setName(dto.getName());
        brand.setLogo(dto.getLogo());
        brand.setFirstLetter(dto.getName().substring(0, 1).toUpperCase());

        this.save(brand);

        saveCategoryRelations(brand.getId(), dto.getCategoryIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBrand(BrandUpdateDto dto) {
        Brand existing = this.getById(dto.getId());
        if (existing == null) {
            throw new BusinessException("品牌不存在");
        }

        boolean nameExists = this.lambdaQuery()
                .eq(Brand::getName, dto.getName())
                .ne(Brand::getId, dto.getId())
                .exists();

        if (nameExists) {
            throw new BusinessException("品牌名称已存在");
        }

        existing.setName(dto.getName());
        existing.setLogo(dto.getLogo());
        existing.setFirstLetter(dto.getName().substring(0, 1).toUpperCase());

        this.updateById(existing);

        categoryBrandMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CategoryBrand>()
                        .eq(CategoryBrand::getBrandId, dto.getId())
        );

        if (!dto.getCategoryIds().isEmpty()) {
            saveCategoryRelations(dto.getId(), dto.getCategoryIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBrand(Long brandId) {
        Brand brand = this.getById(brandId);
        if (brand == null) {
            return;
        }

        this.removeById(brandId);

        categoryBrandMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CategoryBrand>()
                        .eq(CategoryBrand::getBrandId, brandId)
        );
    }

    private BrandDTO convertToDto(Brand brand) {
        BrandDTO dto = new BrandDTO();
        dto.setId(brand.getId());
        dto.setName(brand.getName());
        dto.setLogo(brand.getLogo());
        dto.setFirstLetter(brand.getFirstLetter());

        List<Long> categoryIds = categoryBrandMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CategoryBrand>()
                                .eq(CategoryBrand::getBrandId, brand.getId())
                ).stream()
                .map(CategoryBrand::getCategoryId)
                .collect(Collectors.toList());

        dto.setCategoryIds(categoryIds);
        dto.setCreateTime(brand.getCreateTime());
        dto.setUpdateTime(brand.getUpdateTime());
        return dto;
    }

    private void saveCategoryRelations(Long brandId, List<Long> categoryIds) {
        List<CategoryBrand> relations = categoryIds.stream()
                .map(categoryId -> {
                    CategoryBrand cb = new CategoryBrand();
                    cb.setBrandId(brandId);
                    cb.setCategoryId(categoryId);
                    return cb;
                })
                .collect(Collectors.toList());

        categoryBrandMapper.insertBatchSomeColumn(relations);
    }

    @Override
    public BrandDetailDto getBrandById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("品牌ID不能为空");
        }

        Brand brand = brandMapper.selectById(id);
        if (brand == null) {
            throw new BusinessException("品牌不存在");
        }

        BrandDetailDto dto = new BrandDetailDto();
        dto.setId(brand.getId());
        dto.setName(brand.getName());
        dto.setLogo(brand.getLogo());
        dto.setFirstLetter(brand.getFirstLetter());

        List<Long> categoryIds = getCategoryIdsByBrandId(id);
        dto.setCategoryIds(categoryIds);

        return dto;
    }

    private List<Long> getCategoryIdsByBrandId(Long brandId) {
        return categoryBrandMapper.selectList(
                        new LambdaQueryWrapper<CategoryBrand>()
                                .eq(CategoryBrand::getBrandId, brandId)
                ).stream()
                .map(CategoryBrand::getCategoryId)
                .collect(Collectors.toList());
    }
}
