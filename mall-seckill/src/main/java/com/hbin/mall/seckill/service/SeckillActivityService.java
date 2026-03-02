package com.hbin.mall.seckill.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.api.order.dto.SeckillResult;
import com.hbin.mall.api.product.dto.SkuDTO;
import com.hbin.mall.api.seckill.dto.SeckillActivityCreateRequest;
import com.hbin.mall.api.seckill.dto.SeckillActivityDTO;
import com.hbin.mall.api.seckill.dto.SeckillActivityEditDTO;
import com.hbin.mall.api.seckill.dto.SeckillActivityUpdateRequest;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.seckill.domain.SeckillActivity;
import com.hbin.mall.seckill.dto.SeckillInfoDTO;
import com.hbin.mall.seckill.dto.SeckillRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author hbin
 * @since 2026-01-07
 */
public interface SeckillActivityService extends IService<SeckillActivity> {

    Result<Boolean> seckill(SeckillRequest request);

    void preloadSeckillStock(Long activityId);

    SeckillInfoDTO getSeckillInfoBySpuId(Long spuId);

    Page<SeckillActivityDTO> getSeckillActivities(Integer pageNum, Integer pageSize);

    // SeckillActivityDTO getSeckillActivity(Long activityId);

    SeckillActivityDTO getSeckillActivity(Long activityId);

    SeckillActivityEditDTO getSeckillActivityForEdit(Long activityId);

    @Transactional(rollbackFor = Exception.class)
    void endSeckillActivity(Long activityId);

    List<SkuDTO> getSpuSkusForSeckill(Long spuId);

    @Transactional(rollbackFor = Exception.class)
    void createSeckillActivity(SeckillActivityCreateRequest request);

    @Transactional(rollbackFor = Exception.class)
    void updateSeckillActivity(SeckillActivityUpdateRequest request);

    @Transactional(rollbackFor = Exception.class)
    void deleteSeckillActivity(Long activityId);
}
