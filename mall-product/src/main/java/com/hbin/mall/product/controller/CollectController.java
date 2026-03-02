package com.hbin.mall.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.common.satoken.StpMemberUtil;
import com.hbin.mall.product.domain.ProductCollect;
import com.hbin.mall.product.dto.CollectCreateDTO;
import com.hbin.mall.product.dto.CollectDTO;
import com.hbin.mall.product.service.ProductCollectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product/collect")
@RequiredArgsConstructor
public class CollectController {

    private final ProductCollectService collectService;

    @GetMapping("/list")
    public Result<Page<CollectDTO>> getCollectList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        Page<CollectDTO> page = collectService.getCollectList(memberId, pageNum, pageSize);
        return Result.success(page);
    }

    @PostMapping
    public Result<String> collectProduct(@Valid @RequestBody CollectCreateDTO dto) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        collectService.collectProduct(memberId, dto);
        return Result.success("收藏成功");
    }

    @PostMapping("/cancel-by-spu")
    public Result<String> cancelCollectBySpuId(@RequestBody Map<String, Long> request) {
        Long spuId = request.get("spuId");
        if (spuId == null) {
            throw new IllegalArgumentException("spuId不能为空");
        }

        Long memberId = StpMemberUtil.getLoginIdAsLong();
        collectService.cancelCollectBySpuId(memberId, spuId);
        return Result.success("取消收藏成功");
    }

    @DeleteMapping("/{collectId}")
    public Result<String> cancelCollect(@PathVariable Long collectId) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        collectService.cancelCollect(memberId, collectId);
        return Result.success("取消收藏成功");
    }

    @PostMapping("/batch-cancel")
    public Result<String> batchCancelCollect(@RequestBody List<Long> collectIds) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        collectService.batchCancelCollect(memberId, collectIds);
        return Result.success("批量取消收藏成功");
    }

    @GetMapping("/check/{spuId}")
    public Result<Boolean> checkCollected(@PathVariable Long spuId) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        Boolean collected = collectService.checkCollected(memberId, spuId);
        return Result.success(collected);
    }

    @GetMapping("/count")
    public Result<Long> getCollectCount() {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        Long count = collectService.getCollectCount(memberId);
        return Result.success(count);
    }
}