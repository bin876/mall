package com.hbin.mall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.file.feign.FileFeignClient;
import com.hbin.mall.api.product.dto.BannerDTO;
import com.hbin.mall.api.product.dto.BannerFormDTO;
import com.hbin.mall.api.product.dto.BannerQueryDTO;
import com.hbin.mall.api.product.feign.BannerFeignClient;
import com.hbin.mall.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/banner")
@RequiredArgsConstructor
public class BannerController {

    @Autowired
    private BannerFeignClient bannerFeignClient;

    @Autowired
    private FileFeignClient fileFeignClient;

    @PostMapping("/list")
    public Result<Page<BannerDTO>> getBannerList(@RequestBody BannerQueryDTO query) {
        return bannerFeignClient.getBannerList(query);
    }

    @PostMapping
    public Result<Void> create(@RequestBody BannerFormDTO dto) {
        return bannerFeignClient.create(dto);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody BannerFormDTO dto) {
        return bannerFeignClient.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return bannerFeignClient.delete(id);
    }

    @PostMapping("/image")
    public Result<String> uploadSpuImage(@RequestPart MultipartFile file) {
        return fileFeignClient.upload(file, "banner");
    }
}