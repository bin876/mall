package com.hbin.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.api.product.dto.BannerDTO;
import com.hbin.mall.api.product.dto.BannerFormDTO;
import com.hbin.mall.api.product.dto.BannerQueryDTO;
import com.hbin.mall.common.exception.BusinessException;
import com.hbin.mall.product.domain.Banner;
import com.hbin.mall.product.mapper.BannerMapper;
import com.hbin.mall.product.service.BannerService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Override
    public Page<BannerDTO> getPage(BannerQueryDTO query) {
        Page<Banner> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        if (query.getStatus() != null) {
            wrapper.eq(Banner::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(Banner::getSort);
        IPage<Banner> result = this.page(page, wrapper);
        return (Page<BannerDTO>) result.convert(this::convertToDTO);
    }

    @Override
    public void create(BannerFormDTO dto) {
        Banner banner = new Banner();
        banner.setTitle(dto.getTitle());
        banner.setImageUrl(dto.getImageUrl());
        banner.setTargetUrl(dto.getTargetUrl());
        banner.setSort(dto.getSort() == null ? 0 : dto.getSort());
        banner.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        this.save(banner);
    }

    @Override
    public void update(Long id, BannerFormDTO dto) {
        Banner banner = this.getById(id);
        if (banner == null) throw new BusinessException("轮播图不存在");
        banner.setTitle(dto.getTitle());
        banner.setImageUrl(dto.getImageUrl());
        banner.setTargetUrl(dto.getTargetUrl());
        banner.setSort(dto.getSort());
        banner.setStatus(dto.getStatus());
        this.updateById(banner);
    }

    @Override
    public void delete(Long id) {
        this.removeById(id);
    }

    @Override
    public List<Banner> getActiveBanners() {
        return this.list(
                new LambdaQueryWrapper<Banner>()
                        .eq(Banner::getStatus, 1)
                        .orderByAsc(Banner::getSort)
        );
    }

    private BannerDTO convertToDTO(Banner banner) {
        BannerDTO dto = new BannerDTO();
        BeanUtils.copyProperties(banner, dto);
        return dto;
    }
}
