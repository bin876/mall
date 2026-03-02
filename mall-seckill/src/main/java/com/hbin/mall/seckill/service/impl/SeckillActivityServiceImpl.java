package com.hbin.mall.seckill.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.api.product.dto.SkuDTO;
import com.hbin.mall.api.product.dto.SpuDTO;
import com.hbin.mall.api.product.feign.SkuFeignClient;
import com.hbin.mall.api.product.feign.SpuFeignClient;
import com.hbin.mall.api.seckill.dto.*;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.seckill.config.RabbitMqConfig;
import com.hbin.mall.seckill.domain.SeckillActivity;
import com.hbin.mall.seckill.domain.SeckillSku;
import com.hbin.mall.seckill.dto.SeckillInfoDTO;
import com.hbin.mall.seckill.dto.SeckillOrderMessage;
import com.hbin.mall.seckill.dto.SeckillRequest;
import com.hbin.mall.seckill.mapper.SeckillActivityMapper;
import com.hbin.mall.seckill.mapper.SeckillSkuMapper;
import com.hbin.mall.seckill.service.SeckillActivityService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeckillActivityServiceImpl extends ServiceImpl<SeckillActivityMapper, SeckillActivity> implements SeckillActivityService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SeckillSkuMapper seckillSkuMapper;

    @Autowired
    private SeckillActivityMapper seckillActivityMapper;

    @Autowired
    private SkuFeignClient skuFeignClient;

    @Autowired
    private SpuFeignClient spuFeignClient;

    @Override
    @SentinelResource(value = "seckill", blockHandler = "handleFlowControl")
    public Result<Boolean> seckill(SeckillRequest request) {
        try {
            if (request.getSkuId() == null || request.getUserId() == null) {
                return Result.error("参数错误");
            }

            if (hasUserSeckilled(request.getUserId(), request.getSkuId())) {
                return Result.error("您已参与过本次秒杀");
            }

            SeckillSku seckillSku = getSeckillSku(request.getSkuId());
            if (seckillSku == null) {
                return Result.error("秒杀商品不存在");
            }

            if (!isActivityActive(seckillSku.getActivityId())) {
                return Result.error("秒杀活动未开始或已结束");
            }

            String soldOutKey = "seckill:soldout:" + request.getSkuId();
            Boolean isSoldOut = Boolean.valueOf(redisTemplate.opsForValue().get(soldOutKey));
            if (Boolean.TRUE.equals(isSoldOut)) {
                return Result.error("商品已售罄");
            }

            String stockKey = "seckill:stock:" + request.getSkuId();
            Long newStock = redisTemplate.opsForValue().decrement(stockKey);

            if (newStock == null || newStock < 0) {
                redisTemplate.opsForValue().set(soldOutKey, "true", Duration.ofHours(1));
                redisTemplate.opsForValue().increment(stockKey);
                return Result.error("手慢了，商品已售罄");
            }

            recordUserSeckill(request.getUserId(), request.getSkuId());

            SeckillOrderMessage message = new SeckillOrderMessage();
            message.setSkuId(request.getSkuId());
            message.setUserId(request.getUserId());
            message.setSeckillPrice(seckillSku.getSeckillPrice());
            message.setCreateTime(LocalDateTime.now());

            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.SECKILL_ORDER_EXCHANGE,
                    RabbitMqConfig.SECKILL_ORDER_ROUTING_KEY,
                    message
            );

            return Result.success(true, "秒杀成功，正在生成订单");

        } catch (Exception e) {
            return Result.error("系统繁忙，请稍后重试");
        }
    }

    public Result<Boolean> handleFlowControl(SeckillRequest request, BlockException ex) {
        return Result.error("系统繁忙，请稍后再试");
    }

    private boolean hasUserSeckilled(Long userId, Long skuId) {
        String key = "seckill:user:" + userId + ":sku:" + skuId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private void recordUserSeckill(Long userId, Long skuId) {
        String key = "seckill:user:" + userId + ":sku:" + skuId;
        redisTemplate.opsForValue().set(key, "1", Duration.ofHours(24));
    }

    private SeckillSku getSeckillSku(Long skuId) {
        return seckillSkuMapper.selectOne(
                new LambdaQueryWrapper<SeckillSku>().eq(SeckillSku::getSkuId, skuId)
        );
    }

    private boolean isActivityActive(Long activityId) {
        if (activityId == null || activityId <= 0) {
            return false;
        }

        SeckillActivity activity = seckillActivityMapper.selectById(activityId);
        if (activity == null) {
            return false;
        }

        if (activity.getStatus() != null && activity.getStatus() != 0) {
            return activity.getStatus() == 1;
        }

        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(activity.getStartTime()) &&
                now.isBefore(activity.getEndTime());
    }

    @Override
    public void preloadSeckillStock(Long activityId) {
        List<SeckillSku> seckillSkus = seckillSkuMapper.selectList(
                new LambdaQueryWrapper<SeckillSku>().eq(SeckillSku::getActivityId, activityId)
        );

        for (SeckillSku sku : seckillSkus) {
            String stockKey = "seckill:stock:" + sku.getSkuId();
            String soldOutKey = "seckill:soldout:" + sku.getSkuId();

            redisTemplate.opsForValue().set(stockKey, String.valueOf(sku.getAvailableStock()));
            redisTemplate.delete(soldOutKey);
        }
    }


    @Override
    public SeckillInfoDTO getSeckillInfoBySpuId(Long spuId) {
        List<SeckillSku> seckillSkus = seckillSkuMapper.selectList(
                new LambdaQueryWrapper<SeckillSku>()
                        .eq(SeckillSku::getSpuId, spuId)
                        .exists("SELECT 1 FROM seckill_activity sa " +
                                "WHERE sa.id = seckill_sku.activity_id " +
                                "AND sa.start_time <= NOW() AND sa.end_time >= NOW()")
        );

        if (seckillSkus.isEmpty()) {
            return null;
        }
        SeckillSku seckillSku = seckillSkus.get(0);
        SeckillActivity activity = seckillActivityMapper.selectById(seckillSku.getActivityId());

        SeckillInfoDTO info = new SeckillInfoDTO();
        info.setActivityId(seckillSku.getActivityId());
        info.setSkuId(seckillSku.getSkuId());
        info.setSeckillPrice(seckillSku.getSeckillPrice());
        info.setTotalStock(seckillSku.getTotalStock());
        info.setAvailableStock(seckillSku.getAvailableStock());
        info.setStartTime(activity.getStartTime());
        info.setEndTime(activity.getEndTime());
        info.setStatus(getActivityStatus(activity));

        return info;
    }

    private Integer getActivityStatus(SeckillActivity activity) {
        if (activity.getStatus() != null) {
            return (int) activity.getStatus();
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            return 0; // 未开始
        }
        if (now.isAfter(activity.getEndTime())) {
            return 2; // 已结束
        }
        return 1; // 进行中
    }


    @Override
    public Page<SeckillActivityDTO> getSeckillActivities(Integer pageNum, Integer pageSize) {
        Page<SeckillActivity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SeckillActivity::getStartTime);

        IPage<SeckillActivity> activityPage = this.page(page, wrapper);

        List<SeckillActivityDTO> dtoList = activityPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return (Page<SeckillActivityDTO>) new Page<SeckillActivityDTO>()
                .setCurrent(activityPage.getCurrent())
                .setSize(activityPage.getSize())
                .setTotal(activityPage.getTotal())
                .setPages(activityPage.getPages())
                .setRecords(dtoList);
    }

    @Override
    public SeckillActivityDTO getSeckillActivity(Long activityId) {
        SeckillActivity activity = this.getById(activityId);
        if (activity == null) {
            return null;
        }
        return convertToDTO(activity);
    }

    @Override
    public SeckillActivityEditDTO getSeckillActivityForEdit(Long activityId) {
        SeckillActivity activity = this.getById(activityId);
        if (activity == null) {
            throw new IllegalArgumentException("活动不存在");
        }

        if (getActivityStatus(activity) != 0) {
            throw new IllegalArgumentException("只有未开始的活动才能编辑");
        }

        return convertToEditDTO(activity);
    }

    private SeckillActivityEditDTO convertToEditDTO(SeckillActivity activity) {
        SeckillActivityEditDTO dto = new SeckillActivityEditDTO();
        dto.setId(activity.getId());
        dto.setName(activity.getName());
        dto.setStartTime(activity.getStartTime());
        dto.setEndTime(activity.getEndTime());
        dto.setStatus(activity.getStatus());
        dto.setCanEdit(true); // 未开始的活动可以编辑
        dto.setCanAddSkus(true); // 未开始的活动可以添加SKU
        dto.setCanRemoveSkus(true); // 未开始的活动可以移除SKU

        List<SeckillSku> seckillSkus = seckillSkuMapper.selectList(
                new LambdaQueryWrapper<SeckillSku>().eq(SeckillSku::getActivityId, activity.getId())
        );

        List<SeckillSkuEditDTO> skuEditDTOs = seckillSkus.stream()
                .map(this::convertSkuToEditDTO)
                .collect(Collectors.toList());

        dto.setSkus(skuEditDTOs);
        return dto;
    }

    private SeckillSkuEditDTO convertSkuToEditDTO(SeckillSku seckillSku) {
        SeckillSkuEditDTO dto = new SeckillSkuEditDTO();
        dto.setId(seckillSku.getId());
        dto.setActivityId(seckillSku.getActivityId());
        dto.setSpuId(seckillSku.getSpuId());
        dto.setSkuId(seckillSku.getSkuId());
        dto.setSeckillPrice(seckillSku.getSeckillPrice());
        dto.setTotalStock(seckillSku.getTotalStock());
        dto.setAvailableStock(seckillSku.getAvailableStock());
        dto.setOriginalStock(seckillSku.getAvailableStock());

        try {
            Result<SkuDTO> skuResult = skuFeignClient.getSkuById(seckillSku.getSkuId());
            if (skuResult != null && skuResult.getData() != null) {
                SkuDTO sku = skuResult.getData();
                dto.setSkuName(sku.getName());
                dto.setDefaultImg(sku.getDefaultImg());
                dto.setOriginalPrice(sku.getPrice());
                dto.setMaxStock(sku.getStock());

                dto.setSaleAttrs(convertSaleAttrs(sku.getSaleAttrs()));

                dto.setCanEditPrice(true);
                dto.setPriceLimitMessage("秒杀价格必须低于原价且不低于原价的50%");

                dto.setCanEditStock(true);
                dto.setStockLimitMessage(String.format("库存不能超过商品总库存(%d)", sku.getStock()));

                Result<SpuDTO> spuResult = spuFeignClient.getSPUById(seckillSku.getSpuId());
                if (spuResult != null && spuResult.getData() != null) {
                    dto.setSpuName(spuResult.getData().getName());
                }
            }
        } catch (Exception e) {
            dto.setCanEditPrice(false);
            dto.setCanEditStock(false);
            dto.setPriceLimitMessage("获取商品信息失败，无法验证价格");
            dto.setStockLimitMessage("获取商品信息失败，无法验证库存");
        }

        return dto;
    }

    private List<SeckillSkuEditDTO.SaleAttrDTO> convertSaleAttrs(List<SkuDTO.SaleAttrDTO> saleAttrs) {
        if (saleAttrs == null || saleAttrs.isEmpty()) {
            return Collections.emptyList();
        }

        return saleAttrs.stream()
                .map(attr -> {
                    SeckillSkuEditDTO.SaleAttrDTO result = new SeckillSkuEditDTO.SaleAttrDTO();
                    result.setAttrId(attr.getAttrId());
                    result.setAttrName(attr.getAttrName());
                    result.setAttrValue(attr.getAttrValue());
                    return result;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endSeckillActivity(Long activityId) {
        SeckillActivity activity = this.getById(activityId);
        if (activity == null) {
            throw new IllegalArgumentException("活动不存在");
        }

        activity.setEndTime(LocalDateTime.now());
        activity.setStatus((byte) 2);
        this.updateById(activity);
    }

    @Override
    public List<SkuDTO> getSpuSkusForSeckill(Long spuId) {
        Result<List<SkuDTO>> result = skuFeignClient.getSkusBySpuId(spuId);
        if (result != null && result.getData() != null) {
            return result.getData();
        }
        return Collections.emptyList();
    }

    private SeckillActivityDTO convertToDTO(SeckillActivity activity) {
        SeckillActivityDTO dto = new SeckillActivityDTO();
        dto.setId(activity.getId());
        dto.setName(activity.getName());
        dto.setStartTime(activity.getStartTime());
        dto.setEndTime(activity.getEndTime());
        dto.setStatus(activity.getStatus());

        List<SeckillSku> seckillSkus = seckillSkuMapper.selectList(
                new LambdaQueryWrapper<SeckillSku>().eq(SeckillSku::getActivityId, activity.getId())
        );

        List<SeckillSkuDTO> skuDTOs = seckillSkus.stream()
                .map(this::convertSkuToDTO)
                .collect(Collectors.toList());

        dto.setSkus(skuDTOs);
        return dto;
    }

    private SeckillSkuDTO convertSkuToDTO(SeckillSku seckillSku) {
        SeckillSkuDTO dto = new SeckillSkuDTO();
        dto.setId(seckillSku.getId());
        dto.setActivityId(seckillSku.getActivityId());
        dto.setSpuId(seckillSku.getSpuId());
        dto.setSkuId(seckillSku.getSkuId());
        dto.setSeckillPrice(seckillSku.getSeckillPrice());
        dto.setTotalStock(seckillSku.getTotalStock());
        dto.setAvailableStock(seckillSku.getAvailableStock());

        try {
            Result<SkuDTO> skuResult = skuFeignClient.getSkuById(seckillSku.getSkuId());
            if (skuResult != null && skuResult.getData() != null) {
                SkuDTO sku = skuResult.getData();
                dto.setSkuName(sku.getName());
                dto.setDefaultImg(sku.getDefaultImg());
                dto.setPrice(sku.getPrice());

                Result<SpuDTO> spuResult = spuFeignClient.getSPUById(seckillSku.getSpuId());
                if (spuResult != null && spuResult.getData() != null) {
                    dto.setSpuName(spuResult.getData().getName());
                }
            }
        } catch (Exception e) {
        }

        return dto;
    }

    @Scheduled(fixedRate = 30000)
    public void autoUpdateActivityStatus() {
        LocalDateTime now = LocalDateTime.now();

        List<SeckillActivity> toStart = seckillActivityMapper.selectList(
                new LambdaQueryWrapper<SeckillActivity>()
                        .eq(SeckillActivity::getStatus, 0)
                        .le(SeckillActivity::getStartTime, now)
                        .ge(SeckillActivity::getEndTime, now)
        );

        List<SeckillActivity> toEnd = seckillActivityMapper.selectList(
                new LambdaQueryWrapper<SeckillActivity>()
                        .eq(SeckillActivity::getStatus, 1)
                        .lt(SeckillActivity::getEndTime, now)
        );

        for (SeckillActivity activity : toStart) {
            activity.setStatus((byte) 1);
            seckillActivityMapper.updateById(activity);

            preloadSeckillStock(activity.getId());
        }

        for (SeckillActivity activity : toEnd) {
            activity.setStatus((byte) 2);
            seckillActivityMapper.updateById(activity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSeckillActivity(SeckillActivityCreateRequest request) {
        validateActivityRequest(request);

        SeckillActivity activity = new SeckillActivity();
        activity.setName(request.getName());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setStatus((byte) 0);

        this.save(activity);

        createSeckillSkus(activity.getId(), request.getSkus());
    }

    private void createSeckillSkus(Long activityId, List<SeckillSkuCreateDTO> skuRequests) {
        for (SeckillSkuCreateDTO request : skuRequests) {
            SeckillSku seckillSku = new SeckillSku();
            seckillSku.setActivityId(activityId);
            seckillSku.setSpuId(request.getSpuId());
            seckillSku.setSkuId(request.getSkuId());
            seckillSku.setSeckillPrice(request.getSeckillPrice());
            seckillSku.setTotalStock(request.getStock());
            seckillSku.setAvailableStock(request.getStock());

            seckillSkuMapper.insert(seckillSku);
        }
    }

    private void validateActivityRequest(SeckillActivityCreateRequest request) {
        if (StringUtils.isBlank(request.getName())) {
            throw new IllegalArgumentException("活动名称不能为空");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("活动时间不能为空");
        }
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }
        if (request.getSkus() == null || request.getSkus().isEmpty()) {
            throw new IllegalArgumentException("至少需要选择一个商品");
        }

        for (SeckillSkuCreateDTO sku : request.getSkus()) {
            if (sku.getSkuId() == null) {
                throw new IllegalArgumentException("商品SKU不能为空");
            }
            if (sku.getSeckillPrice() == null || sku.getSeckillPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("秒杀价格必须大于0");
            }
            if (sku.getStock() == null || sku.getStock() <= 0) {
                throw new IllegalArgumentException("库存数量必须大于0");
            }

            Result<SkuDTO> skuResult = skuFeignClient.getSkuById(sku.getSkuId());
            if (skuResult == null || skuResult.getData() == null) {
                throw new IllegalArgumentException("商品SKU不存在: " + sku.getSkuId());
            }

            SkuDTO skuDTO = skuResult.getData();
            // if (!(sku.getStock() > skuDTO.getStock())) {
            //     throw new IllegalArgumentException("设置的库存数量(" + sku.getStock() +
            //             ")不能大于商品实际库存(" + skuDTO.getStock() + "): " + sku.getSkuId());
            // }

            if (sku.getSeckillPrice().compareTo(skuDTO.getPrice().multiply(BigDecimal.valueOf(0.5))) < 0) {
                throw new IllegalArgumentException("秒杀价格不能低于原价的50%: " + sku.getSkuId());
            }
            if (sku.getSeckillPrice().compareTo(skuDTO.getPrice()) >= 0) {
                throw new IllegalArgumentException("秒杀价格必须低于原价: " + sku.getSkuId());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSeckillActivity(SeckillActivityUpdateRequest request) {
        SeckillActivity existing = this.getById(request.getId());
        if (existing == null) {
            throw new IllegalArgumentException("活动不存在");
        }

        if (getActivityStatus(existing) != 0) {
            throw new IllegalArgumentException("只有未开始的活动才能编辑");
        }

        validateActivityRequest(request);

        checkTimeConflictExcludingSelf(request.getStartTime(), request.getEndTime(), request.getId());

        existing.setName(request.getName());
        existing.setStartTime(request.getStartTime());
        existing.setEndTime(request.getEndTime());
        this.updateById(existing);

        seckillSkuMapper.delete(new LambdaQueryWrapper<SeckillSku>()
                .eq(SeckillSku::getActivityId, request.getId()));

        createSeckillSkus(request.getId(), request.getSkus());

        preloadSeckillStock(request.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSeckillActivity(Long activityId) {
        SeckillActivity activity = this.getById(activityId);
        if (activity == null) {
            throw new IllegalArgumentException("活动不存在");
        }

        int status = getActivityStatus(activity);
        if (status == 1) {
            throw new IllegalArgumentException("进行中的活动不允许删除");
        }

        List<SeckillSku> skus = seckillSkuMapper.selectList(
                new LambdaQueryWrapper<SeckillSku>().eq(SeckillSku::getActivityId, activityId)
        );

        for (SeckillSku sku : skus) {
            String stockKey = "seckill:stock:" + sku.getSkuId();
            String soldOutKey = "seckill:soldout:" + sku.getSkuId();
            redisTemplate.delete(stockKey);
            redisTemplate.delete(soldOutKey);
        }

        seckillSkuMapper.delete(new LambdaQueryWrapper<SeckillSku>()
                .eq(SeckillSku::getActivityId, activityId));

        this.removeById(activityId);
    }

    private void checkTimeConflictExcludingSelf(LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(SeckillActivity::getId, excludeId);
        wrapper.and(w -> w
                .between(SeckillActivity::getStartTime, startTime, endTime)
                .or()
                .between(SeckillActivity::getEndTime, startTime, endTime)
                .or()
                .le(SeckillActivity::getStartTime, startTime)
                .ge(SeckillActivity::getEndTime, endTime)
        );

        List<SeckillActivity> conflicts = this.list(wrapper);
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("活动时间与现有活动冲突，请调整时间");
        }
    }

    private void validateActivityRequest(SeckillActivityUpdateRequest request) {
        SeckillActivityCreateRequest createRequest = new SeckillActivityCreateRequest();
        createRequest.setName(request.getName());
        createRequest.setStartTime(request.getStartTime());
        createRequest.setEndTime(request.getEndTime());

        List<SeckillSkuCreateDTO> createSkus = request.getSkus().stream()
                .map(sku -> {
                    SeckillSkuCreateDTO dto = new SeckillSkuCreateDTO();
                    dto.setSpuId(sku.getSpuId());
                    dto.setSkuId(sku.getSkuId());
                    dto.setSeckillPrice(sku.getSeckillPrice());
                    dto.setStock(sku.getStock());
                    return dto;
                })
                .collect(Collectors.toList());

        createRequest.setSkus(createSkus);

        validateActivityRequest(createRequest);
    }
}
