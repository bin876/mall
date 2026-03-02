package com.hbin.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.api.member.dto.MemberAddressDTO;
import com.hbin.mall.api.member.feign.MemberFeignClient;
import com.hbin.mall.api.order.dto.*;
import com.hbin.mall.api.order.dto.OrderDetailDTO;
import com.hbin.mall.api.order.dto.OrderItemDTO;
import com.hbin.mall.api.product.dto.DeductStockRequest;
import com.hbin.mall.api.product.dto.LockStockRequest;
import com.hbin.mall.api.product.dto.ReleaseStockRequest;
import com.hbin.mall.api.product.dto.SkuDTO;
import com.hbin.mall.api.product.feign.SkuFeignClient;
import com.hbin.mall.common.exception.BusinessException;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.common.satoken.StpMemberUtil;
import com.hbin.mall.order.domain.*;
import com.hbin.mall.order.dto.*;
import com.hbin.mall.order.enums.OrderStatusEnum;
import com.hbin.mall.order.enums.PayTypeEnum;
import com.hbin.mall.order.mapper.*;
import com.hbin.mall.order.service.OrderService;
import com.hbin.mall.order.util.CouponCalculator;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static cn.hutool.core.lang.Console.log;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private SkuFeignClient skuFeignClient;

    @Autowired
    private MemberFeignClient memberFeignClient;

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Autowired
    private CouponTemplateMapper couponTemplateMapper;

    @Autowired
    private CouponCalculator couponCalculator;

    @Override
    @Transactional(readOnly = true)
    public IPage<OrderSimpleDTO> getOrderList(Long memberId, OrderListQuery query) {
        Page<Order> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getMemberId, memberId);
        if (query.getStatus() != null) {
            wrapper.eq(Order::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(Order::getCreateTime);

        IPage<Order> orderPage = orderMapper.selectPage(page, wrapper);

        List<Long> orderIds = orderPage.getRecords().stream()
                .map(Order::getOrderId)
                .collect(Collectors.toList());

        Map<Long, Integer> productCountMap = getProductCountMap(orderIds);
        Map<Long, String> firstProductImgMap = getFirstProductImgMap(orderIds);

        List<OrderSimpleDTO> dtoList = orderPage.getRecords().stream().map(order -> {
            OrderSimpleDTO dto = convertToSimpleDTO(order);
            dto.setProductCount(productCountMap.getOrDefault(order.getOrderId(), 0));
            dto.setFirstProductImg(firstProductImgMap.getOrDefault(order.getOrderId(), ""));
            return dto;
        }).collect(Collectors.toList());

        return new Page<OrderSimpleDTO>()
                .setCurrent(orderPage.getCurrent())
                .setSize(orderPage.getSize())
                .setTotal(orderPage.getTotal())
                .setPages(orderPage.getPages())
                .setRecords(dtoList);
    }

    @Override
    public OrderConfirmResponse confirmOrder(OrderConfirmRequest request) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        List<OrderSkuDTO> skuList = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Long> skuIdsToLock = new ArrayList<>();
        List<Integer> quantitiesToLock = new ArrayList<>();

        try {
            if (request.getCartIds() != null && !request.getCartIds().isEmpty()) {
                List<Cart> carts = cartMapper.selectBatchIds(request.getCartIds());
                carts = carts.stream()
                        .filter(cart -> cart.getMemberId().equals(memberId))
                        .collect(Collectors.toList());

                for (Cart cart : carts) {
                    skuList.add(convertToOrderSku(cart));
                    totalAmount = totalAmount.add(
                            cart.getSkuPrice().multiply(BigDecimal.valueOf(cart.getQuantity()))
                    );
                    skuIdsToLock.add(cart.getSkuId());
                    quantitiesToLock.add(cart.getQuantity());
                }
            } else if (request.getSkuId() != null) {
                OrderSkuDTO skuDto = new OrderSkuDTO();
                skuDto.setSkuId(request.getSkuId());

                Result<SkuDTO> skuResult = skuFeignClient.getSkuById(request.getSkuId());
                if (skuResult.getData() == null) {
                    throw new IllegalArgumentException("商品不存在");
                }
                SkuDTO sku = skuResult.getData();

                skuDto.setSkuName(sku.getName());
                skuDto.setSkuPrice(sku.getPrice());
                skuDto.setSkuPic(sku.getDefaultImg());
                skuDto.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1);
                skuList.add(skuDto);

                totalAmount = sku.getPrice().multiply(
                        BigDecimal.valueOf(skuDto.getQuantity())
                );

                skuIdsToLock.add(request.getSkuId());
                quantitiesToLock.add(skuDto.getQuantity());
            } else {
                throw new IllegalArgumentException("参数错误");
            }

            lockStock(skuIdsToLock, quantitiesToLock);

            OrderConfirmResponse response = new OrderConfirmResponse();
            response.setSkus(skuList);
            response.setTotalAmount(totalAmount);
            response.setPayAmount(totalAmount);
            return response;

        } catch (Exception e) {
            releaseLockedStock(skuIdsToLock, quantitiesToLock);
            throw e;
        }
    }

    private void lockStock(List<Long> skuIds, List<Integer> quantities) {
        if (skuIds.size() != quantities.size()) {
            throw new IllegalArgumentException("SKU ID和数量列表长度不匹配");
        }

        for (int i = 0; i < skuIds.size(); i++) {
            Long skuId = skuIds.get(i);
            Integer quantity = quantities.get(i);

            LockStockRequest request = new LockStockRequest();
            request.setSkuId(skuId);
            request.setQuantity(quantity);

            Result<Boolean> result = skuFeignClient.lockStock(request);
            if (!Boolean.TRUE.equals(result.getData())) {
                throw new RuntimeException("库存不足，无法锁定商品");
            }
        }
    }

    private void releaseLockedStock(List<Long> skuIds, List<Integer> quantities) {
        if (skuIds.isEmpty()) return;

        for (int i = 0; i < skuIds.size(); i++) {
            Long skuId = skuIds.get(i);
            Integer quantity = quantities.get(i);

            ReleaseStockRequest request = new ReleaseStockRequest();
            request.setSkuId(skuId);
            request.setQuantity(quantity);

            try {
                skuFeignClient.releaseStock(request);
            } catch (Exception ignored) {
            }
        }
    }

    private OrderSkuDTO convertToOrderSku(Cart cart) {
        OrderSkuDTO dto = new OrderSkuDTO();
        dto.setSkuId(cart.getSkuId());
        dto.setSkuName(cart.getSkuName());
        dto.setSkuPrice(cart.getSkuPrice());
        dto.setSkuPic(cart.getSkuPic());
        dto.setQuantity(cart.getQuantity());
        return dto;
    }

    @Override
    @GlobalTransactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();

        List<Long> skuIdsToLock = new ArrayList<>();
        List<Integer> quantitiesToLock = new ArrayList<>();

        try {
            List<OrderSkuDTO> orderSkus = getOrderSkus(request, memberId);

            for (OrderSkuDTO sku : orderSkus) {
                skuIdsToLock.add(sku.getSkuId());
                quantitiesToLock.add(sku.getQuantity());
            }

            String orderSn = generateOrderSn(memberId);

            Order order = buildOrder(request, memberId, orderSn, orderSkus);
            orderMapper.insert(order);

            // 核销优惠券
            if (order.getCouponCode() != null) {
                UserCoupon coupon = new UserCoupon();
                coupon.setCouponCode(order.getCouponCode());
                coupon.setStatus(1);
                coupon.setUseTime(LocalDateTime.now());
                coupon.setOrderSn(order.getOrderSn());
                userCouponMapper.update(coupon, new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getCouponCode, order.getCouponCode()));
            }

            List<OrderItem> orderItems = buildOrderItems(order, orderSkus);
            orderItemMapper.insertBatchSomeColumn(orderItems);

            if (request.getCartIds() != null) {
                cartMapper.deleteBatchIds(request.getCartIds());
            }

            CreateOrderResponse response = new CreateOrderResponse();
            response.setOrderId(order.getOrderId());
            response.setOrderSn(orderSn);
            return response;

        } catch (Exception e) {
            releaseLockedStock(skuIdsToLock, quantitiesToLock);
            throw e;
        }
    }

    private List<OrderSkuDTO> getOrderSkus(CreateOrderRequest request, Long memberId) {
        if (request.getCartIds() != null) {
            // 购物车
            List<Cart> carts = cartMapper.selectBatchIds(request.getCartIds());
            return carts.stream()
                    .filter(cart -> cart.getMemberId().equals(memberId))
                    .map(this::convertToOrderSku)
                    .collect(Collectors.toList());
        } else {
            // 立即购买
            Result<SkuDTO> skuResult = skuFeignClient.getSkuById(request.getSkuId());
            if (skuResult.getData() == null) {
                throw new IllegalArgumentException("商品不存在");
            }
            SkuDTO sku = skuResult.getData();

            OrderSkuDTO dto = new OrderSkuDTO();
            dto.setSkuId(sku.getSkuId());
            dto.setSkuName(sku.getName());
            dto.setSkuPrice(sku.getPrice());
            dto.setSkuPic(sku.getDefaultImg());
            dto.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1);
            return List.of(dto);
        }
    }

    private String generateOrderSn(Long memberId) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String userIdPart = org.apache.commons.lang3.StringUtils.leftPad(String.valueOf(memberId % 10000), 4, '0');
        String randomPart = RandomStringUtils.randomNumeric(4);
        return "ORDER" + time + userIdPart + randomPart;
    }

    private Order buildOrder(CreateOrderRequest request, Long memberId, String orderSn, List<OrderSkuDTO> skus) {
        Order order = new Order();

        order.setOrderSn(orderSn);
        order.setMemberId(memberId);
        String username = memberFeignClient.getMemberUsername(memberId).getData();
        order.setUsername(username);
        order.setTotalAmount(skus.stream()
                .map(sku -> sku.getSkuPrice().multiply(BigDecimal.valueOf(sku.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        order.setPayAmount(order.getTotalAmount());
        order.setStatus(0);
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverProvince(request.getReceiverProvince());
        order.setReceiverCity(request.getReceiverCity());
        order.setReceiverRegion(request.getReceiverRegion());
        order.setReceiverDetailAddress(request.getReceiverDetailAddress());
        order.setCreateTime(LocalDateTime.now());

        BigDecimal totalAmount = skus.stream()
                .map(sku -> sku.getSkuPrice().multiply(BigDecimal.valueOf(sku.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);

        // 处理优惠券
        if (request.getCouponCode() != null) {
            UserCoupon coupon = userCouponMapper.selectOne(
                    new LambdaQueryWrapper<UserCoupon>()
                            .eq(UserCoupon::getCouponCode, request.getCouponCode())
                            .eq(UserCoupon::getMemberId, memberId)
                            .eq(UserCoupon::getStatus, 0)
            );

            if (coupon == null) {
                throw new IllegalArgumentException("优惠券无效");
            }

            if (coupon.getExpireTime().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("优惠券已过期");
            }

            CouponTemplate template = couponTemplateMapper.selectById(coupon.getTemplateId());
            if (!couponCalculator.isValid(template, totalAmount)) {
                throw new IllegalArgumentException("订单金额不满足优惠券使用条件");
            }

            BigDecimal discount = couponCalculator.calculateDiscount(template, totalAmount);
            order.setCouponCode(request.getCouponCode());
            order.setCouponDiscount(discount);
            order.setPayAmount(totalAmount.subtract(discount));
        } else {
            order.setPayAmount(totalAmount);
        }

        return order;
    }

    private List<OrderItem> buildOrderItems(Order order, List<OrderSkuDTO> skus) {
        return skus.stream().map(sku -> {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getOrderId());
            item.setOrderSn(order.getOrderSn());
            item.setMemberId(order.getMemberId());
            item.setSkuId(sku.getSkuId());
            item.setSkuName(sku.getSkuName());
            item.setSkuPrice(sku.getSkuPrice());
            item.setSkuPic(sku.getSkuPic());
            item.setQuantity(sku.getQuantity());
            return item;
        }).collect(Collectors.toList());
    }

    private OrderSimpleDTO convertToSimpleDTO(Order order) {
        OrderSimpleDTO dto = new OrderSimpleDTO();
        dto.setOrderId(order.getOrderId());
        dto.setOrderSn(order.getOrderSn());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPayAmount(order.getPayAmount());
        dto.setStatus(order.getStatus());
        dto.setStatusDesc(OrderStatusEnum.getDescByCode(order.getStatus()));
        dto.setCreateTime(order.getCreateTime());
        return dto;
    }

    private Map<Long, Integer> getProductCountMap(List<Long> orderIds) {
        if (orderIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Map<String, Object>> counts = orderItemMapper.selectProductCountByOrderIds(orderIds);
        return counts.stream()
                .collect(Collectors.toMap(
                        map -> (Long) map.get("order_id"),
                        map -> ((Long) map.get("count")).intValue()
                ));
    }

    private Map<Long, String> getFirstProductImgMap(List<Long> orderIds) {
        if (orderIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Map<String, Object>> imgs = orderItemMapper.selectFirstProductImgByOrderIds(orderIds);
        return imgs.stream()
                .collect(Collectors.toMap(
                        map -> (Long) map.get("order_id"),
                        map -> (String) map.get("sku_pic")
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailDTO getOrderDetail(Long memberId, String orderSn) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderSn, orderSn)
                        .eq(Order::getMemberId, memberId)
        );

        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderSn, orderSn)
        );

        OrderDetailDTO dto = new OrderDetailDTO();

        dto.setOrderId(order.getOrderId());
        dto.setOrderSn(order.getOrderSn());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setFreightAmount(order.getFreightAmount());
        dto.setPromotionAmount(order.getPromotionAmount());
        dto.setPayAmount(order.getPayAmount());
        dto.setStatus(order.getStatus());
        dto.setStatusDesc(OrderStatusEnum.getDescByCode(order.getStatus()));
        dto.setPayType(order.getPayType());
        dto.setPayTypeDesc(PayTypeEnum.getDescByCode(order.getPayType()));
        dto.setCreateTime(order.getCreateTime());
        dto.setPayTime(order.getPayTime());
        dto.setDeliveryTime(order.getDeliveryTime());
        dto.setReceiveTime(order.getReceiveTime());
        dto.setCloseTime(order.getCloseTime());

        dto.setReceiverName(order.getReceiverName());
        dto.setReceiverPhone(order.getReceiverPhone());
        dto.setReceiverPostCode(order.getReceiverPostCode());
        dto.setReceiverProvince(order.getReceiverProvince());
        dto.setReceiverCity(order.getReceiverCity());
        dto.setReceiverRegion(order.getReceiverRegion());
        dto.setReceiverDetailAddress(order.getReceiverDetailAddress());
        dto.setAutoConfirmDay(order.getAutoConfirmDay());
        dto.setNote(order.getNote());

        dto.setOrderItems(items.stream().map(this::convertToItemDTO).collect(Collectors.toList()));

        return dto;
    }


    @Override
    @Transactional
    public void cancelOrder(Long memberId, String orderSn) {
        Order order = validateOrderOwnership(memberId, orderSn);

        if (order.getStatus() != OrderStatusEnum.UNPAID.getCode()) {
            throw new IllegalArgumentException("只能取消待付款订单");
        }

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderSn, orderSn)
        );

        for (OrderItem item : items) {
            ReleaseStockRequest request = new ReleaseStockRequest();
            request.setSkuId(item.getSkuId());
            request.setQuantity(item.getQuantity());
            skuFeignClient.releaseStock(request);
        }

        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setCloseTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void confirmReceipt(Long memberId, String orderSn) {
        Order order = validateOrderOwnership(memberId, orderSn);

        if (order.getStatus() != OrderStatusEnum.DELIVERED.getCode()) {
            throw new IllegalArgumentException("只能确认已发货的订单");
        }

        order.setStatus(OrderStatusEnum.COMPLETED.getCode());
        order.setReceiveTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long memberId, String orderSn) {
        Order order = validateOrderOwnership(memberId, orderSn);

        Integer status = order.getStatus();
        if (status == null ||
                (status != OrderStatusEnum.COMPLETED.getCode() &&
                        status != OrderStatusEnum.CLOSED.getCode() &&
                        status != OrderStatusEnum.CANCELLED.getCode())) {
            throw new IllegalArgumentException("只能删除已完成、已关闭或已取消的订单");
        }

        orderItemMapper.delete(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderSn, orderSn)
        );

        orderMapper.deleteById(order.getOrderId());
    }

    private Order validateOrderOwnership(Long memberId, String orderSn) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderSn, orderSn)
                        .eq(Order::getMemberId, memberId)
        );

        if (order == null) {
            throw new IllegalArgumentException("订单不存在或无权操作");
        }
        return order;
    }

    @Override
    public Page<OrderDTO> getOrderList(OrderQueryDTO query) {
        Page<Order> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (org.springframework.util.StringUtils.hasText(query.getOrderSn())) {
            wrapper.eq(Order::getOrderSn, query.getOrderSn());
        }
        if (query.getMemberId() != null) {
            wrapper.eq(Order::getMemberId, query.getMemberId());
        }
        if (org.springframework.util.StringUtils.hasText(query.getUsername())) {
            wrapper.like(Order::getUsername, query.getUsername());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Order::getStatus, query.getStatus());
        }
        if (org.springframework.util.StringUtils.hasText(query.getStartTime())) {
            wrapper.ge(Order::getCreateTime, query.getStartTime());
        }
        if (org.springframework.util.StringUtils.hasText(query.getEndTime())) {
            wrapper.le(Order::getCreateTime, query.getEndTime());
        }

        wrapper.orderByDesc(Order::getCreateTime);

        IPage<Order> orderPage = orderMapper.selectPage(page, wrapper);

        return (Page<OrderDTO>) orderPage.convert(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderDetail(Long id) {
        Order order = this.getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        return convertToDTO(order);
    }

    @Override
    public void updateOrderStatus(Long id, Integer status) {
        Order order = this.getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (status == 2) { // 发货
            if (order.getStatus() != 1) {
                throw new BusinessException("只有已付款的订单才能发货");
            }
            order.setDeliveryTime(LocalDateTime.now());
        } else if (status == 4) { // 关闭
            if (order.getStatus() != 0 && order.getStatus() != 1) {
                throw new BusinessException("只有待付款或已付款的订单才能关闭");
            }
            order.setCloseTime(LocalDateTime.now());
        } else if (status == 3) { // 完成
            if (order.getStatus() != 2) {
                throw new BusinessException("只有已发货的订单才能完成");
            }
            order.setReceiveTime(LocalDateTime.now());
        } else if (status == 5) { // 取消
            if (order.getStatus() != 0) {
                throw new BusinessException("只有待付款的订单才能取消");
            }
            order.setCloseTime(LocalDateTime.now());
        }

        order.setStatus(status);
        this.updateById(order);
    }

    @Override
    public void deleteOrder(Long id) {
        Order order = this.getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (order.getStatus() != 3 && order.getStatus() != 4 && order.getStatus() != 5) {
            throw new BusinessException("只能删除已完成、已关闭或已取消的订单");
        }

        this.removeById(id);
        orderItemMapper.deleteByOrderId(id);
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setOrderSn(order.getOrderSn());
        dto.setMemberId(order.getMemberId());
        dto.setUsername(order.getUsername());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPayAmount(order.getPayAmount());
        dto.setStatus(order.getStatus());
        dto.setStatusDesc(getStatusDesc(order.getStatus()));
        dto.setReceiverName(order.getReceiverName());
        dto.setReceiverPhone(order.getReceiverPhone());
        dto.setReceiverProvince(order.getReceiverProvince());
        dto.setReceiverCity(order.getReceiverCity());
        dto.setReceiverRegion(order.getReceiverRegion());
        dto.setReceiverDetailAddress(order.getReceiverDetailAddress());
        dto.setCreateTime(order.getCreateTime());

        List<OrderItem> items = orderItemMapper.selectListByOrderId(order.getOrderId());
        dto.setOrderItems(items.stream()
                .map(this::convertToItemDTO)
                .collect(Collectors.toList()));

        return dto;
    }

    private OrderItemDTO convertToItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setItemId(item.getItemId());
        dto.setSkuId(item.getSkuId());
        dto.setSkuName(item.getSkuName());
        dto.setSkuPic(item.getSkuPic());
        dto.setSkuPrice(item.getSkuPrice());
        dto.setQuantity(item.getQuantity());
        dto.setPromotionName(item.getPromotionName());
        return dto;
    }

    private String getStatusDesc(Integer status) {
        if (status == null) return "未知状态";
        switch (status) {
            case 0: return "待付款";
            case 1: return "已付款";
            case 2: return "已发货";
            case 3: return "已完成";
            case 4: return "已关闭";
            case 5: return "已取消";
            default: return "未知状态";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSeckillOrder(CreateSeckillOrderRequest request) {
        Long memberId = request.getUserId();

        Result<SkuDTO> skuResult = skuFeignClient.getSkuById(request.getSkuId());
        if (skuResult.getData() == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        SkuDTO sku = skuResult.getData();

        String username = getMemberUsername(memberId);

        Result<MemberAddressDTO> addressResult = memberFeignClient.getDefaultAddress(memberId);
        if (addressResult == null || addressResult.getData() == null) {
            throw new IllegalArgumentException("请先设置默认收货地址");
        }
        MemberAddressDTO address = addressResult.getData();

        lockStock(Collections.singletonList(request.getSkuId()), Collections.singletonList(1));

        String orderSn = generateSeckillOrderSn();

        Order order = buildSeckillOrder(request, orderSn, username, sku, address);
        orderMapper.insert(order);

        OrderItem item = buildOrderItem(order, sku, request.getSeckillPrice());
        orderItemMapper.insert(item);
    }

    private Order buildSeckillOrder(CreateSeckillOrderRequest request, String orderSn,
                                    String username, SkuDTO sku, MemberAddressDTO address) {
        Order order = new Order();
        order.setOrderSn(orderSn);
        order.setMemberId(request.getUserId());
        order.setUsername(username);
        order.setTotalAmount(request.getSeckillPrice());
        order.setPayAmount(request.getSeckillPrice());
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());

        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverRegion(address.getRegion());
        order.setReceiverDetailAddress(address.getDetailAddress());
        order.setReceiverPostCode(address.getPostCode());

        order.setFreightAmount(BigDecimal.ZERO);
        order.setPromotionAmount(BigDecimal.ZERO);
        order.setAutoConfirmDay(7);
        order.setNote("秒杀订单");

        return order;
    }

    private String generateSeckillOrderSn() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = RandomStringUtils.randomNumeric(6);
        return "SECKILL" + time + randomPart;
    }

    private OrderItem buildOrderItem(Order order, SkuDTO sku, BigDecimal seckillPrice) {
        OrderItem item = new OrderItem();
        item.setOrderId(order.getOrderId());
        item.setOrderSn(order.getOrderSn());
        item.setMemberId(order.getMemberId());
        item.setSkuId(sku.getSkuId());
        item.setSkuName(sku.getName());
        item.setSkuPrice(seckillPrice);
        item.setSkuPic(sku.getDefaultImg());
        item.setQuantity(1);
        return item;
    }

    private String getMemberUsername(Long memberId) {
        try {
            Result<String> result = memberFeignClient.getMemberUsername(memberId);
            if (result != null && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
        }
        return "用户-" + memberId;
    }
}
