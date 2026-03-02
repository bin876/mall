package com.hbin.mall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.product.dto.BatchUpdateSkuDTO;
import com.hbin.mall.api.product.dto.SkuDTO;
import com.hbin.mall.api.product.dto.SkuQueryDTO;
import com.hbin.mall.api.product.dto.UpdateSkuDTO;
import com.hbin.mall.api.product.feign.SkuFeignClient;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/sku")
public class SkuController {

    @Autowired
    private SkuFeignClient skuFeignClient;

    @PostMapping("/list")
    public Result<Page<SkuDTO>> getSkuList(@Valid @RequestBody SkuQueryDTO query) {
        return skuFeignClient.getSkuList(query);
    }

    @PutMapping("/{id}")
    public Result<String> updateSku(
            @NotNull @PathVariable Long id,
            @Valid @RequestBody UpdateSkuDTO dto) {
        return skuFeignClient.updateSku(id, dto);
    }

    @PutMapping("/batch")
    public Result<String> batchUpdateSku(@Valid @RequestBody BatchUpdateSkuDTO dto) {
        return skuFeignClient.batchUpdateSku(dto);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteSku(@NotNull @PathVariable Long id) {
        return skuFeignClient.deleteSku(id);
    }
}