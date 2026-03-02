package com.hbin.mall.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.api.product.feign.SkuFeignClient;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.seckill.domain.SeckillSku;
import com.hbin.mall.seckill.dto.SeckillOrderMessage;
import com.hbin.mall.seckill.dto.SeckillRequest;
import com.hbin.mall.seckill.mapper.SeckillSkuMapper;
import com.hbin.mall.seckill.service.SeckillSkuService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static cn.hutool.core.lang.Console.log;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hbin
 * @since 2026-01-07
 */
@Service
public class SeckillSkuServiceImpl extends ServiceImpl<SeckillSkuMapper, SeckillSku> implements SeckillSkuService {

}
