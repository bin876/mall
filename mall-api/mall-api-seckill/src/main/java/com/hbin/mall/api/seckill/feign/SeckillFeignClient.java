package com.hbin.mall.api.seckill.feign;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.product.dto.SkuDTO;
import com.hbin.mall.api.seckill.dto.SeckillActivityCreateRequest;
import com.hbin.mall.api.seckill.dto.SeckillActivityDTO;
import com.hbin.mall.api.seckill.dto.SeckillActivityEditDTO;
import com.hbin.mall.api.seckill.dto.SeckillActivityUpdateRequest;
import com.hbin.mall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "mall-seckill", contextId = "seckill")
public interface SeckillFeignClient {

    @GetMapping("/activities")
    public Result<Page<SeckillActivityDTO>> getSeckillActivities(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize);

    /**
     * 获取秒杀活动详情
     */
    // @GetMapping("/activities/{activityId}")
    // public Result<SeckillActivityDTO> getSeckillActivity(@PathVariable Long activityId);

    @GetMapping("/activities/{activityId}/edit")
    Result<SeckillActivityEditDTO> getSeckillActivityForEdit(@PathVariable Long activityId);

    /**
     * 预热秒杀库存
     */
    @PostMapping("/activities/{activityId}/preload")
    public Result<String> preloadSeckillStock(@PathVariable Long activityId);

    /**
     * 手动结束秒杀活动
     */
    @PostMapping("/activities/{activityId}/end")
    public Result<String> endSeckillActivity(@PathVariable Long activityId);

    @GetMapping("/spu/{spuId}/skus")
    public Result<List<SkuDTO>> getSpuSkusForSeckill(@PathVariable Long spuId);

    @PostMapping("/inner/seckill/preload/{activityId}")
    Result<String> preloadStock(@PathVariable Long activityId);

    @PostMapping("/inner/seckill/activities")
    Result<String> createSeckillActivity(@RequestBody SeckillActivityCreateRequest request);

    @PutMapping("/inner/seckill/activities/{activityId}")
    Result<String> updateSeckillActivity(
            @PathVariable("activityId") Long activityId,
            @RequestBody SeckillActivityUpdateRequest request);

    @DeleteMapping("/inner/seckill/activities/{activityId}")
    Result<String> deleteSeckillActivity(@PathVariable("activityId") Long activityId);
}
