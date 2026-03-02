package com.hbin.mall.api.order.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.order.dto.*;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "mall-order", contextId = "order")
public interface OrderFeignClient {

    @PostMapping("/inner/order/seckill")
    Result<String> createSeckillOrder(@Valid @RequestBody CreateSeckillOrderRequest request);

    @PostMapping("/inner/order/list")
    Result<Page<OrderDTO>> getOrderList(@Valid @RequestBody OrderQueryDTO query);

    @GetMapping("/inner/order/{id}")
    Result<OrderDTO> getOrderDetail(@NotNull @PathVariable Long id);

    @PutMapping("/inner/order/{id}/status")
    Result<String> updateOrderStatus(
            @NotNull @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusDTO dto);

    @DeleteMapping("/inner/order/{id}")
    Result<String> deleteOrder(@NotNull @PathVariable Long id);
}
