package com.hbin.mall.seckill.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.product.dto.SkuDTO;
import com.hbin.mall.api.seckill.dto.SeckillActivityCreateRequest;
import com.hbin.mall.api.seckill.dto.SeckillActivityDTO;
import com.hbin.mall.api.seckill.dto.SeckillActivityEditDTO;
import com.hbin.mall.api.seckill.dto.SeckillActivityUpdateRequest;
import com.hbin.mall.api.seckill.feign.SeckillFeignClient;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.seckill.service.SeckillActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AdminSeckillController implements SeckillFeignClient {

    @Autowired
    private SeckillActivityService seckillActivityService;

    @Override
    public Result<Page<SeckillActivityDTO>> getSeckillActivities(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Page<SeckillActivityDTO> page = seckillActivityService.getSeckillActivities(pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("获取秒杀活动列表失败");
        }
    }

    @Override
    public Result<SeckillActivityEditDTO> getSeckillActivityForEdit(@PathVariable Long activityId) {
        try {
            SeckillActivityEditDTO activity = seckillActivityService.getSeckillActivityForEdit(activityId);
            return Result.success(activity);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("获取活动编辑详情失败");
        }
    }

    @Override
    public Result<String> preloadSeckillStock(@PathVariable Long activityId) {
        try {
            seckillActivityService.preloadSeckillStock(activityId);
            return Result.success("库存预热成功");
        } catch (Exception e) {
            return Result.error("库存预热失败");
        }
    }

    @Override
    public Result<String> endSeckillActivity(@PathVariable Long activityId) {
        try {
            seckillActivityService.endSeckillActivity(activityId);
            return Result.success("秒杀活动已结束");
        } catch (Exception e) {
            return Result.error("结束秒杀活动失败");
        }
    }

    @Override
    public Result<List<SkuDTO>> getSpuSkusForSeckill(@PathVariable Long spuId) {
        try {
            List<SkuDTO> skus = seckillActivityService.getSpuSkusForSeckill(spuId);
            return Result.success(skus);
        } catch (Exception e) {
            return Result.error("获取商品SKU失败");
        }
    }

    @Override
    public Result<String> preloadStock(@PathVariable Long activityId) {
        return preloadSeckillStock(activityId);
    }

    @Override
    public Result<String> createSeckillActivity(@RequestBody SeckillActivityCreateRequest request) {
        try {
            seckillActivityService.createSeckillActivity(request);
            return Result.success("秒杀活动创建成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("创建秒杀活动失败");
        }
    }

    @Override
    public Result<String> updateSeckillActivity(@PathVariable Long activityId,
                                                @RequestBody SeckillActivityUpdateRequest request) {
        try {
            SeckillActivityDTO activity = seckillActivityService.getSeckillActivity(activityId);
            if (activity == null) {
                return Result.error("活动不存在");
            }

            request.setId(activityId);
            seckillActivityService.updateSeckillActivity(request);
            return Result.success("秒杀活动更新成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新秒杀活动失败");
        }
    }

    @Override
    public Result<String> deleteSeckillActivity(@PathVariable Long activityId) {
        try {
            SeckillActivityDTO activity = seckillActivityService.getSeckillActivity(activityId);
            if (activity == null) {
                return Result.error("活动不存在");
            }

            seckillActivityService.deleteSeckillActivity(activityId);
            return Result.success("秒杀活动删除成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除秒杀活动失败");
        }
    }

}