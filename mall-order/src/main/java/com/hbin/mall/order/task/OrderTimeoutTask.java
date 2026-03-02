package com.hbin.mall.order.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hbin.mall.api.product.dto.ReleaseStockRequest;
import com.hbin.mall.api.product.feign.SkuFeignClient;
import com.hbin.mall.order.domain.Order;
import com.hbin.mall.order.domain.OrderItem;
import com.hbin.mall.order.enums.OrderStatusEnum;
import com.hbin.mall.order.mapper.OrderItemMapper;
import com.hbin.mall.order.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static cn.dev33.satoken.SaManager.log;
import static cn.hutool.core.lang.Console.log;

@Component
public class OrderTimeoutTask {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private SkuFeignClient skuFeignClient;

    @Scheduled(fixedRate = 300000)
    public void checkTimeoutOrders() {
        log("关闭订单");
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(30);
        
        List<Order> timeoutOrders = orderMapper.selectList(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, OrderStatusEnum.UNPAID.getCode())
                .le(Order::getCreateTime, timeoutTime)
        );
        
        for (Order order : timeoutOrders) {
            try {
                // 释放库存
                List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getOrderId())
                );
                
                for (OrderItem item : items) {
                    ReleaseStockRequest request = new ReleaseStockRequest();
                    request.setSkuId(item.getSkuId());
                    request.setQuantity(item.getQuantity());
                    skuFeignClient.releaseStock(request);
                }
                
                // 更新订单状态
                order.setStatus(OrderStatusEnum.CLOSED.getCode());
                order.setCloseTime(LocalDateTime.now());
                orderMapper.updateById(order);
                
                log.info("订单超时关闭，释放库存: orderSn={}", order.getOrderSn());
                
            } catch (Exception e) {
                log.error("处理超时订单失败: orderSn={}", order.getOrderSn(), e);
            }
        }
    }
}