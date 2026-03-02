package com.hbin.mall.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.order.dto.*;
import com.hbin.mall.api.order.feign.OrderFeignClient;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.order.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static cn.dev33.satoken.SaManager.log;

@RestController
public class AdminOrderController implements OrderFeignClient {

    @Autowired
    private OrderService orderService;

    @Override
    public Result<String> createSeckillOrder(@Valid @RequestBody CreateSeckillOrderRequest request) {
        try {
            orderService.createSeckillOrder(request);
            return Result.success("秒杀订单创建成功");
        } catch (Exception e) {
            log.error("创建秒杀订单失败", e);
            return Result.error("创建秒杀订单失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Page<OrderDTO>> getOrderList(@Valid @RequestBody OrderQueryDTO query) {
        Page<OrderDTO> result = orderService.getOrderList(query);
        return Result.success(result);
    }

    @Override
    public Result<OrderDTO> getOrderDetail(@NotNull @PathVariable Long id) {
        OrderDTO order = orderService.getOrderDetail(id);
        return Result.success(order);
    }

    @Override
    public Result<String> updateOrderStatus(
            @NotNull @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusDTO dto) {
        orderService.updateOrderStatus(id, dto.getStatus());
        return Result.success("订单状态更新成功");
    }

    @Override
    public Result<String> deleteOrder(@NotNull @PathVariable Long id) {
        orderService.deleteOrder(id);
        return Result.success("订单删除成功");
    }
}