package com.hbin.mall.api.product.feign;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.product.dto.*;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "mall-product", contextId = "spu")
public interface SpuFeignClient {

    @PostMapping("/inner/spu")
    Result<String> publishSpu(@Valid @RequestBody PublishSpuRequest request);

    @PostMapping("/inner/spu/list")
    Result<Page<SpuDTO>> getSpuList(@Valid @RequestBody SpuQueryDTO query);

    @GetMapping("/inner/spu/{id}")
    Result<SpuDetailDTO> getSpuById(@NotNull @PathVariable Long id);

    @PutMapping
    Result<String> updateSpu(@Valid @RequestBody UpdateSpuDTO dto);

    @PutMapping("/inner/spu/{id}/status")
    Result<String> updateSpuStatus(
            @NotNull @PathVariable Long id,
            @RequestBody UpdateSpuStatusDTO dto);

    @DeleteMapping("/inner/spu/{id}")
    Result<String> deleteSpu(@NotNull @PathVariable Long id);

    @GetMapping("/inner/spu/name/{spuId}")
    Result<String> getSPUName(@PathVariable Long spuId);

    @GetMapping("/inner/spu/seckill/{spuId}")
    Result<SpuDTO> getSPUById(@PathVariable Long spuId);
}
