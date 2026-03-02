package com.hbin.mall.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.product.dto.*;
import com.hbin.mall.api.product.feign.SpuFeignClient;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.product.service.SpuService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdminProductController implements SpuFeignClient {

    @Autowired
    private SpuService spuService;

    @Override
    public Result<String> publishSpu(@Valid @RequestBody PublishSpuRequest request) {
        spuService.publishSpu(request);
        return Result.success("商品发布成功");
    }

    @Override
    public Result<Page<SpuDTO>> getSpuList(@Valid @RequestBody SpuQueryDTO query) {
        Page<SpuDTO> result = spuService.getSpuList(query);
        return Result.success(result);
    }

    @Override
    public Result<SpuDetailDTO> getSpuById(@NotNull @PathVariable Long id) {
        SpuDetailDTO spu = spuService.getSpuDetail(id);
        return Result.success(spu);
    }

    @PutMapping
    public Result<String> updateSpu(@Valid @RequestBody UpdateSpuDTO dto) {
        spuService.updateSpu(dto);
        return Result.success("商品更新成功");
    }

    @Override
    public Result<String> updateSpuStatus(
            @NotNull @PathVariable Long id,
            @RequestBody UpdateSpuStatusDTO dto) {
        spuService.updateSpuStatus(id, dto.getPublishStatus());
        return Result.success("状态更新成功");
    }

    @Override
    public Result<String> deleteSpu(@NotNull @PathVariable Long id) {
        spuService.deleteSpu(id);
        return Result.success("商品删除成功");
    }

    @Override
    public Result<String> getSPUName(Long spuId) {
        try {
            String name = spuService.getSPUName(spuId);
            if (name != null) {
                return Result.success(name);
            }
            return Result.error("SPU不存在");
        } catch (Exception e) {
            return Result.error("获取SPU名称失败");
        }
    }

    @Override
    public Result<SpuDTO> getSPUById(@PathVariable Long spuId) {
        try {
            SpuDTO spuDTO = spuService.getSPUById(spuId);
            if (spuDTO != null) {
                return Result.success(spuDTO);
            }
            return Result.error("SPU不存在");
        } catch (Exception e) {
            return Result.error("获取SPU详情失败");
        }
    }
}
