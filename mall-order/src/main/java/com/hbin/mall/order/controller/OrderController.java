package com.hbin.mall.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hbin.mall.api.order.dto.CreateOrderResponse;
import com.hbin.mall.api.order.dto.OrderDetailDTO;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.common.satoken.StpMemberUtil;
import com.hbin.mall.order.domain.UserCoupon;
import com.hbin.mall.order.dto.*;
import com.hbin.mall.order.mapper.UserCouponMapper;
import com.hbin.mall.order.service.OrderService;
import com.hbin.mall.order.service.UserCouponService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/list")
    public Result<IPage<OrderSimpleDTO>> getOrderList(@RequestBody OrderListQuery query) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();

        IPage<OrderSimpleDTO> page = orderService.getOrderList(memberId, query);
        return Result.success(page);
    }

    @PostMapping("/confirm")
    public Result<OrderConfirmResponse> confirmOrder(@Valid @RequestBody OrderConfirmRequest request) {
        OrderConfirmResponse response = orderService.confirmOrder(request);
        return Result.success(response);
    }

    @PostMapping("/create")
    public Result<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderResponse response = orderService.createOrder(request);
        return Result.success(response);
    }

    @GetMapping("/{orderSn}")
    public Result<OrderDetailDTO> getOrderDetail(@PathVariable String orderSn) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        OrderDetailDTO detail = orderService.getOrderDetail(memberId, orderSn);
        return Result.success(detail);
    }


    @PutMapping("/{orderSn}/cancel")
    public Result<String> cancelOrder(@PathVariable String orderSn) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        orderService.cancelOrder(memberId, orderSn);
        return Result.success("订单已取消");
    }

    @PutMapping("/{orderSn}/confirm")
    public Result<String> confirmReceipt(@PathVariable String orderSn) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        orderService.confirmReceipt(memberId, orderSn);
        return Result.success("已确认收货");
    }

    @DeleteMapping("/{orderSn}")
    public Result<String> deleteOrder(@PathVariable String orderSn) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        orderService.deleteOrder(memberId, orderSn);
        return Result.success("订单已删除");
    }
}
