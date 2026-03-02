package com.hbin.mall.api.product.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.product.dto.*;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "mall-product", contextId = "sku")
public interface SkuFeignClient {

    @GetMapping("/inner/sku/{skuId}")
    Result<SkuDTO> getSkuById(@PathVariable Long skuId);

    @PostMapping("/inner/sku/lock-stock")
    Result<Boolean> lockStock(@RequestBody LockStockRequest request);

    @PostMapping("/inner/sku/deduct-stock")
    Result<Boolean> deductStock(@RequestBody DeductStockRequest request);

    @GetMapping("/inner/sku/{skuId}/spu-id")
    Result<Long> getSpuIdBySkuId(@PathVariable Long skuId);

    @PostMapping("/inner/sku/release-stock")
    boolean releaseStock(@RequestBody ReleaseStockRequest request);

    @PostMapping("/inner/sku/list")
    Result<Page<SkuDTO>> getSkuList(@Valid @RequestBody SkuQueryDTO query);

    @PutMapping("/inner/sku/{id}")
    Result<String> updateSku(
            @NotNull @PathVariable Long id,
            @Valid @RequestBody UpdateSkuDTO dto);

    @PutMapping("/inner/sku/batch")
    Result<String> batchUpdateSku(@Valid @RequestBody BatchUpdateSkuDTO dto);

    @DeleteMapping("/inner/sku/{id}")
    Result<String> deleteSku(@NotNull @PathVariable Long id);

    @GetMapping("/inner/spu/{spuId}/skus")
    Result<List<SkuDTO>> getSkusBySpuId(@PathVariable Long spuId);
}