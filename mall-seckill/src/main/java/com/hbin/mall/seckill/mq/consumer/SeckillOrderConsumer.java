package com.hbin.mall.seckill.mq.consumer;

import com.hbin.mall.api.order.dto.CreateSeckillOrderRequest;
import com.hbin.mall.api.order.feign.OrderFeignClient;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.seckill.config.RabbitMqConfig;
import com.hbin.mall.seckill.dto.SeckillOrderMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SeckillOrderConsumer {

    @Autowired
    private OrderFeignClient orderFeignClient;
    
    @Autowired
    private StringRedisTemplate redisTemplate;

    @RabbitListener(queues = RabbitMqConfig.SECKILL_ORDER_QUEUE)
    public void handleSeckillOrder(SeckillOrderMessage message) {
        try {
            CreateSeckillOrderRequest orderRequest = new CreateSeckillOrderRequest();
            orderRequest.setSkuId(message.getSkuId());
            orderRequest.setUserId(message.getUserId());
            orderRequest.setSeckillPrice(message.getSeckillPrice());
            
            Result<String> result = orderFeignClient.createSeckillOrder(orderRequest);
            
            if (result.getCode() != 200) {
                log.error("秒杀订单创建失败: {}", result.getMsg());
            }
            
        } catch (Exception e) {
            log.error("秒杀订单处理异常", e);
        }
    }
}