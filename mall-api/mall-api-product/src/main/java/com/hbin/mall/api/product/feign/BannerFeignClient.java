package com.hbin.mall.api.product.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.product.dto.*;
import com.hbin.mall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "mall-product", contextId = "banner")
public interface BannerFeignClient {

    @PostMapping("/inner/banner/list")
    Result<Page<BannerDTO>> getBannerList(@RequestBody BannerQueryDTO query);

    @PostMapping("/inner/banner")
    Result<Void> create(@RequestBody BannerFormDTO dto);

    @PutMapping("/inner/banner/{id}")
    Result<Void> update(@PathVariable Long id, @RequestBody BannerFormDTO dto);

    @DeleteMapping("/inner/banner/{id}")
    Result<Void> delete(@PathVariable Long id);
}
