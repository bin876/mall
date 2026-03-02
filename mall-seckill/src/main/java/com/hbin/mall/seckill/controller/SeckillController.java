package com.hbin.mall.seckill.controller;

import com.hbin.mall.common.result.Result;
import com.hbin.mall.common.satoken.StpMemberUtil;
import com.hbin.mall.seckill.dto.SeckillInfoDTO;
import com.hbin.mall.seckill.dto.SeckillRequest;
import com.hbin.mall.seckill.service.SeckillActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static cn.dev33.satoken.SaManager.log;

@RestController
@RequestMapping("/seckill")
@RequiredArgsConstructor
public class SeckillController {

    @Autowired
    private SeckillActivityService seckillActivityService;

    @GetMapping("/info/{spuId}")
    public Result<SeckillInfoDTO> getSeckillInfo(@PathVariable Long spuId) {
        try {
            SeckillInfoDTO info = seckillActivityService.getSeckillInfoBySpuId(spuId);
            if (info != null) {
                return Result.success(info);
            }
        } catch (Exception e) {
            log.error("获取秒杀信息失败", e);
            return Result.error("获取秒杀信息失败");
        }
        return Result.success();
    }

    @PostMapping("/do")
    public Result<Boolean> doSeckill(@RequestBody SeckillRequest request) {
        Long userId = StpMemberUtil.getLoginIdAsLong();
        request.setUserId(userId);
        return seckillActivityService.seckill(request);
    }
}