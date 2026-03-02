package com.hbin.mall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.order.dto.OrderDTO;
import com.hbin.mall.api.order.dto.OrderQueryDTO;
import com.hbin.mall.api.order.dto.UpdateOrderStatusDTO;
import com.hbin.mall.api.order.feign.OrderFeignClient;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/order")
public class OrderController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    @PostMapping("/list")
    public Result<Page<OrderDTO>> getOrderList(@Valid @RequestBody OrderQueryDTO query) {
        return orderFeignClient.getOrderList(query);
    }

    @GetMapping("/{id}")
    public Result<OrderDTO> getOrderDetail(@NotNull @PathVariable Long id) {
        return orderFeignClient.getOrderDetail(id);
    }

    @PutMapping("/{id}/status")
    public Result<String> updateOrderStatus(
            @NotNull @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusDTO dto) {
        return orderFeignClient.updateOrderStatus(id, dto);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteOrder(@NotNull @PathVariable Long id) {
        return orderFeignClient.deleteOrder(id);
    }
}