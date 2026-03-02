package com.hbin.mall.product.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.product.dto.*;
import com.hbin.mall.api.product.feign.SkuFeignClient;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.product.domain.Sku;
import com.hbin.mall.product.mapper.SkuMapper;
import com.hbin.mall.product.service.SkuService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AdminSkuController implements SkuFeignClient {

    @Autowired
    private SkuService skuService;

    @Autowired
    private SkuMapper skuMapper;

    @Override
    public Result<SkuDTO> getSkuById(@PathVariable Long skuId) {
        return skuService.getSkuById(skuId);
    }

    @Override
    public Result<Boolean> lockStock(@RequestBody LockStockRequest request) {
        boolean success = skuService.lockStock(request.getSkuId(), request.getQuantity());
        return Result.success(success);
    }

    @Override
    public Result<Boolean> deductStock(@RequestBody DeductStockRequest request) {
        boolean success = skuService.deductStock(request.getSkuId(), request.getQuantity());
        return Result.success(success);
    }

    @Override
    public Result<Long> getSpuIdBySkuId(@PathVariable Long skuId) {
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) return Result.error("SKU不存在");
        return Result.success(sku.getSpuId());
    }

    @Override
    @Transactional
    public boolean releaseStock(@RequestBody ReleaseStockRequest request) {
        int updated = skuMapper.update(null,
                new UpdateWrapper<Sku>()
                        .setSql("stock_locked = stock_locked - " + request.getQuantity())
                        .eq("sku_id", request.getSkuId())
                        .ge("stock_locked", request.getQuantity())
        );
        return updated > 0;
    }

    @Override
    public Result<Page<SkuDTO>> getSkuList(@Valid @RequestBody SkuQueryDTO query) {
        Page<SkuDTO> result = skuService.getSkuList(query);
        return Result.success(result);
    }

    @Override
    public Result<String> updateSku(
            @NotNull @PathVariable Long id,
            @Valid @RequestBody UpdateSkuDTO dto) {
        skuService.updateSku(id, dto);
        return Result.success("SKU更新成功");
    }

    @Override
    public Result<String> batchUpdateSku(@Valid @RequestBody BatchUpdateSkuDTO dto) {
        skuService.batchUpdateSku(dto);
        return Result.success("批量更新成功");
    }

    @Override
    public Result<String> deleteSku(@NotNull @PathVariable Long id) {
        skuService.deleteSku(id);
        return Result.success("SKU删除成功");
    }

    @Override
    public Result<List<SkuDTO>> getSkusBySpuId(Long spuId) {
        try {
            List<SkuDTO> skus = skuService.getSkusBySpuId(spuId);
            return Result.success(skus);
        } catch (Exception e) {
            return Result.error("获取商品列表失败");
        }
    }
}