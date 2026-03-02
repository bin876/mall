package com.hbin.mall.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.product.dto.SkuDTO;
import com.hbin.mall.api.seckill.dto.*;
import com.hbin.mall.api.seckill.feign.SeckillFeignClient;
import com.hbin.mall.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/admin/seckill")
@RequiredArgsConstructor
public class SeckillController  {

    @Autowired
    private SeckillFeignClient seckillFeignClient;

    @GetMapping("/activities")
    public Result<Page<SeckillActivityDTO>> getSeckillActivities(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Result<Page<SeckillActivityDTO>> seckillActivities = seckillFeignClient.getSeckillActivities(pageNum, pageSize);
        return seckillActivities;
    }

    /**
     * 获取秒杀活动详情
     */
    // @GetMapping("/activities/{activityId}")
    // public Result<SeckillActivityDTO> getSeckillActivity(@PathVariable Long activityId) {
    //     return  seckillFeignClient.getSeckillActivity(activityId);
    //
    // }
    @GetMapping("/activities/{activityId}/edit")
    public Result<SeckillActivityEditDTO> getSeckillActivityForEdit(@PathVariable Long activityId) {
        return seckillFeignClient.getSeckillActivityForEdit(activityId);
    }

    /**
     * 创建秒杀活动
     */
    @PostMapping("/activities")
    public Result<String> createSeckillActivity(@RequestBody SeckillActivityCreateRequest request) {
        return seckillFeignClient.createSeckillActivity(request);
    }

    /**
     * 预热秒杀库存
     */
    @PostMapping("/activities/{activityId}/preload")
    public Result<String> preloadSeckillStock(@PathVariable Long activityId) {
        return  seckillFeignClient.preloadSeckillStock(activityId);

    }

    /**
     * 手动结束秒杀活动
     */
    @PostMapping("/activities/{activityId}/end")
    public Result<String> endSeckillActivity(@PathVariable Long activityId) {
        return  seckillFeignClient.endSeckillActivity(activityId);
    }

    /**
     * 获取商品的秒杀配置（用于创建/编辑时选择商品）
     */
    @GetMapping("/spu/{spuId}/skus")
    public Result<List<SkuDTO>> getSpuSkusForSeckill(@PathVariable Long spuId) {
        return  seckillFeignClient.getSpuSkusForSeckill(spuId);
    }

    @PutMapping("/activities/{activityId}")
    public Result<String> updateSeckillActivity(@PathVariable Long activityId,
                                                @RequestBody SeckillActivityUpdateRequest request) {
        return  seckillFeignClient.updateSeckillActivity(activityId, request);
    }

    /**
     * 删除秒杀活动
     */
    @DeleteMapping("/activities/{activityId}")
    public Result<String> deleteSeckillActivity(@PathVariable Long activityId) {
         return    seckillFeignClient.deleteSeckillActivity(activityId);
    }
}