package com.hbin.mall.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.product.dto.BannerDTO;
import com.hbin.mall.api.product.dto.BannerFormDTO;
import com.hbin.mall.api.product.dto.BannerQueryDTO;
import com.hbin.mall.api.product.feign.BannerFeignClient;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.product.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdminBannerController implements BannerFeignClient {

    @Autowired
    private BannerService bannerService;

    @Override
    public Result<Page<BannerDTO>> getBannerList(@RequestBody BannerQueryDTO query) {
        return Result.success(bannerService.getPage(query));
    }

    @Override
    public Result<Void> create(@RequestBody BannerFormDTO dto) {
        bannerService.create(dto);
        return Result.success();
    }

    @Override
    public Result<Void> update(@PathVariable Long id, @RequestBody BannerFormDTO dto) {
        bannerService.update(id, dto);
        return Result.success();
    }

    @Override
    public Result<Void> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return Result.success();
    }
}