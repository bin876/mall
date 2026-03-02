package com.hbin.mall.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.product.dto.*;
import com.hbin.mall.api.product.feign.SpuFeignClient;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/spu")
public class SpuController {

    @Autowired
    private SpuFeignClient spuFeignClient;

    @PostMapping("/list")
    public Result<Page<SpuDTO>> getSpuList(@Valid @RequestBody SpuQueryDTO query) {
        return spuFeignClient.getSpuList(query);
    }

    @GetMapping("/{id}")
    public Result<SpuDetailDTO> getSpuById(@PathVariable Long id) {
        return spuFeignClient.getSpuById(id);
    }

    @PutMapping
    public Result<String> updateSpu(@Valid @RequestBody UpdateSpuDTO dto) {
        return spuFeignClient.updateSpu(dto);
    }

    @PutMapping("/{id}/status")
    Result<String> updateSpuStatus(
            @NotNull @PathVariable Long id,
            @RequestBody UpdateSpuStatusDTO dto) {
        return spuFeignClient.updateSpuStatus(id, dto);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteSpu(@PathVariable Long id) {
        return spuFeignClient.deleteSpu(id);
    }
}