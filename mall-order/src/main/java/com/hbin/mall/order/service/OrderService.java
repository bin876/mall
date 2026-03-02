package com.hbin.mall.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.api.order.dto.*;
import com.hbin.mall.api.order.dto.OrderDetailDTO;
import com.hbin.mall.order.domain.Order;
import com.hbin.mall.order.dto.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.transaction.annotation.Transactional;

public interface OrderService extends IService<Order> {

    @Transactional(readOnly = true)
    IPage<OrderSimpleDTO> getOrderList(Long memberId, OrderListQuery query);

    OrderConfirmResponse confirmOrder(OrderConfirmRequest request);

    @GlobalTransactional
    CreateOrderResponse createOrder(CreateOrderRequest request);

    @Transactional(readOnly = true)
    OrderDetailDTO getOrderDetail(Long memberId, String orderSn);

    @Transactional
    void cancelOrder(Long memberId, String orderSn);

    @Transactional
    void confirmReceipt(Long memberId, String orderSn);

    @Transactional
    void deleteOrder(Long memberId, String orderSn);

    Page<OrderDTO> getOrderList(OrderQueryDTO query);

    @Transactional(readOnly = true)
    OrderDTO getOrderDetail(Long id);

    void updateOrderStatus(Long id, Integer status);

    void deleteOrder(Long id);

    @Transactional
    void createSeckillOrder(CreateSeckillOrderRequest request);
}
