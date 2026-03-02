package com.hbin.mall.seckill.controller;

import com.hbin.mall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/seckill/metrics")
public class MetricsController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/stock/{skuId}")
    public Result<Map<String, Long>> getStock(@PathVariable Long skuId) {
        String stockKey = "seckill:stock:" + skuId;
        String stockStr = redisTemplate.opsForValue().get(stockKey);
        Long stock = stockStr != null ? Long.parseLong(stockStr) : 0L;
        
        return Result.success(Map.of("skuId", skuId, "stock", stock));
    }

    @GetMapping("/activity/{activityId}")
    public Map<String, Object> getActivityStatus(@PathVariable Long activityId) {
        return Map.of("activityId", activityId, "status", "active");
    }
}