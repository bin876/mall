package com.hbin.mall.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.api.product.dto.BannerDTO;
import com.hbin.mall.api.product.dto.BannerFormDTO;
import com.hbin.mall.api.product.dto.BannerQueryDTO;
import com.hbin.mall.product.domain.Banner;

import java.util.List;

public interface BannerService extends IService<Banner> {

    Page<BannerDTO> getPage(BannerQueryDTO query);

    void create(BannerFormDTO dto);

    void update(Long id, BannerFormDTO dto);

    void delete(Long id);

    List<Banner> getActiveBanners();
}
