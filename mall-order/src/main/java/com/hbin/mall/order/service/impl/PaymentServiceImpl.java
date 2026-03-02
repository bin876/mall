package com.hbin.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.api.order.dto.PaymentDTO;
import com.hbin.mall.api.order.dto.PaymentDetailDTO;
import com.hbin.mall.api.order.dto.PaymentQueryDTO;
import com.hbin.mall.api.product.dto.DeductStockRequest;
import com.hbin.mall.api.product.feign.SkuFeignClient;
import com.hbin.mall.common.event.NotificationEvent;
import com.hbin.mall.common.exception.BusinessException;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.order.config.RabbitMqConfig;
import com.hbin.mall.order.domain.Order;
import com.hbin.mall.order.domain.OrderItem;
import com.hbin.mall.order.domain.Payment;
import com.hbin.mall.order.dto.PaymentResponse;
import com.hbin.mall.order.dto.SubmitPaymentRequest;
import com.hbin.mall.order.enums.OrderStatusEnum;
import com.hbin.mall.order.enums.PayTypeEnum;
import com.hbin.mall.order.enums.PaymentStatusEnum;
import com.hbin.mall.order.mapper.OrderItemMapper;
import com.hbin.mall.order.mapper.OrderMapper;
import com.hbin.mall.order.mapper.PaymentMapper;
import com.hbin.mall.order.mq.message.SaleCountUpdateMessage;
import com.hbin.mall.order.service.PaymentService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private SkuFeignClient skuFeignClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public PaymentResponse submitPayment(Long memberId, SubmitPaymentRequest request) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderSn, request.getOrderSn())
                        .eq(Order::getMemberId, memberId)
        );

        if (order == null) {
            throw new IllegalArgumentException("订单不存在或无权操作");
        }

        if (order.getStatus() != OrderStatusEnum.UNPAID.getCode()) {
            throw new IllegalStateException("订单状态不可支付");
        }

        order.setStatus(OrderStatusEnum.PAID.getCode());
        order.setPayType(request.getPayType());
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        Payment payment = new Payment();
        payment.setOrderId(order.getOrderId());
        payment.setOrderSn(order.getOrderSn());
        payment.setMemberId(memberId);
        payment.setPaySn("PAY" + System.currentTimeMillis());
        payment.setPayType(request.getPayType());
        payment.setAmount(order.getPayAmount());
        payment.setStatus(PaymentStatusEnum.SUCCESS.getCode());
        payment.setPayTime(LocalDateTime.now());
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());
        paymentMapper.insert(payment);

        List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getOrderId())
        );
        sendSaleCountMessages(orderItems);

        PaymentResponse response = new PaymentResponse();
        response.setOrderSn(order.getOrderSn());

        NotificationEvent event = new NotificationEvent();
        event.setMemberId(memberId);
        event.setTitle("订单支付成功");
        event.setContent("您的订单 " + order.getOrderSn() + " 已支付成功！");
        event.setType(1);
        event.setCategory("order");
        event.setTargetUrl("/user/order/" + order.getOrderSn());
        event.setRelatedId(order.getOrderId());

        rabbitTemplate.convertAndSend(
                "notification.exchange",
                "notification.send",
                event
        );

        for (OrderItem item : orderItems) {
            DeductStockRequest deductRequest = new DeductStockRequest();
            deductRequest.setSkuId(item.getSkuId());
            deductRequest.setQuantity(item.getQuantity());
            skuFeignClient.deductStock(deductRequest);
        }

        return response;
    }

    private void sendSaleCountMessages(List<OrderItem> orderItems) {
        Map<Long, Integer> skuSales = orderItems.stream()
                .collect(Collectors.groupingBy(
                        OrderItem::getSkuId,
                        Collectors.summingInt(OrderItem::getQuantity)
                ));

        Map<Long, Integer> spuSales = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : skuSales.entrySet()) {
            Long skuId = entry.getKey();
            Integer quantity = entry.getValue();

            Result<Long> result = skuFeignClient.getSpuIdBySkuId(skuId);
            if (result.getData() != null) {
                spuSales.merge(result.getData(), quantity, Integer::sum);
            } else {
                log.warn("SKU {} 不存在，跳过销量更新");
            }
        }

        spuSales.forEach((spuId, quantity) -> {
            SaleCountUpdateMessage message = new SaleCountUpdateMessage();
            message.setSpuId(spuId);
            message.setQuantity(quantity);
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.SALE_COUNT_EXCHANGE,
                    RabbitMqConfig.SALE_COUNT_ROUTING_KEY,
                    message
            );
        });
    }

    @Override
    public Page<PaymentDTO> getPaymentList(PaymentQueryDTO query) {
        Page<Payment> page = new Page<>(query.getPageNum(), query.getPageSize());

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Payment> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        if (org.springframework.util.StringUtils.hasText(query.getOrderSn())) {
            wrapper.eq(Payment::getOrderSn, query.getOrderSn());
        }
        if (org.springframework.util.StringUtils.hasText(query.getPaySn())) {
            wrapper.eq(Payment::getPaySn, query.getPaySn());
        }
        if (query.getPayType() != null) {
            wrapper.eq(Payment::getPayType, query.getPayType());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Payment::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(Payment::getCreateTime);

        IPage<Payment> paymentPage = paymentMapper.selectPage(page, wrapper);

        return (Page<PaymentDTO>) paymentPage.convert(this::convertToDTO);
    }

    @Override
    public PaymentDetailDTO getPaymentDetail(Long id) {
        Payment payment = this.getById(id);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }

        return convertToDetailDTO(payment);
    }

    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(payment.getPaymentId());
        dto.setMemberId(payment.getMemberId());
        dto.setOrderId(payment.getOrderId());
        dto.setOrderSn(payment.getOrderSn());
        dto.setPaySn(payment.getPaySn());
        dto.setPayType(payment.getPayType());
        dto.setPayTypeDesc(getPayTypeDesc(payment.getPayType()));
        dto.setAmount(payment.getAmount());
        dto.setStatus(payment.getStatus());
        dto.setStatusDesc(getPaymentStatusDesc(payment.getStatus()));
        dto.setPayTime(payment.getPayTime());
        dto.setCreateTime(payment.getCreateTime());
        dto.setUpdateTime(payment.getUpdateTime());
        return dto;
    }

    private PaymentDetailDTO convertToDetailDTO(Payment payment) {
        PaymentDetailDTO dto = new PaymentDetailDTO();
        dto.setPaymentId(payment.getPaymentId());
        dto.setMemberId(payment.getMemberId());
        dto.setOrderId(payment.getOrderId());
        dto.setOrderSn(payment.getOrderSn());
        dto.setPaySn(payment.getPaySn());
        dto.setPayType(payment.getPayType());
        dto.setPayTypeDesc(getPayTypeDesc(payment.getPayType()));
        dto.setAmount(payment.getAmount());
        dto.setStatus(payment.getStatus());
        dto.setStatusDesc(getPaymentStatusDesc(payment.getStatus()));
        dto.setPayTime(payment.getPayTime());
        dto.setCreateTime(payment.getCreateTime());
        dto.setUpdateTime(payment.getUpdateTime());
        dto.setCallbackContent(payment.getCallbackContent());
        return dto;
    }

    private String getPayTypeDesc(Integer payType) {
        if (payType == null) return "未知";
        switch (payType) {
            case 1: return "支付宝";
            case 2: return "微信";
            case 3: return "银行卡";
            default: return "未知";
        }
    }

    private String getPaymentStatusDesc(Integer status) {
        if (status == null) return "未知状态";
        switch (status) {
            case 0: return "待支付";
            case 1: return "成功";
            case 2: return "失败";
            case 3: return "已退款";
            default: return "未知状态";
        }
    }
}
