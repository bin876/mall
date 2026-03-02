package com.hbin.mall.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.api.product.dto.*;
import com.hbin.mall.product.domain.Brand;
import org.springframework.transaction.annotation.Transactional;

public interface BrandService extends IService<Brand> {

    Page<BrandDTO> getBrandList(BrandQueryDTO query);

    @Transactional(rollbackFor = Exception.class)
    void createBrand(BrandCreateDto dto);

    @Transactional(rollbackFor = Exception.class)
    void updateBrand(BrandUpdateDto dto);

    @Transactional(rollbackFor = Exception.class)
    void deleteBrand(Long brandId);

    BrandDetailDto getBrandById(Long id);
}
