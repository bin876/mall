package com.hbin.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.api.member.feign.MemberFeignClient;
import com.hbin.mall.api.order.dto.CartDTO;
import com.hbin.mall.api.order.dto.CartQueryDTO;
import com.hbin.mall.api.product.dto.SkuDTO;
import com.hbin.mall.api.product.feign.SkuFeignClient;
import com.hbin.mall.common.exception.BusinessException;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.common.satoken.StpMemberUtil;
import com.hbin.mall.order.dto.CartItemDTO;
import com.hbin.mall.order.dto.UpdateCartDTO;
import com.hbin.mall.order.mapper.CartMapper;
import com.hbin.mall.order.domain.Cart;
import com.hbin.mall.order.dto.AddCartDTO;
import com.hbin.mall.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private SkuFeignClient skuFeignClient;

    @Autowired
    private MemberFeignClient memberFeignClient;

    @Override
    @Transactional
    public void addToCart(AddCartDTO dto) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();

        Result<SkuDTO> skuResult = skuFeignClient.getSkuById(dto.getSkuId());
        if (skuResult.getData() == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        SkuDTO sku = skuResult.getData();

        Cart existingCart = cartMapper.selectOne(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getMemberId, memberId)
                        .eq(Cart::getSkuId, dto.getSkuId())
        );

        if (existingCart != null) {
            existingCart.setQuantity(existingCart.getQuantity() + dto.getQuantity());
            cartMapper.updateById(existingCart);
        } else {
            Cart cart = new Cart();
            cart.setMemberId(memberId);
            cart.setSkuId(dto.getSkuId());
            cart.setSkuName(sku.getName());
            cart.setSkuPrice(sku.getPrice());
            cart.setSkuPic(sku.getDefaultImg());
            cart.setQuantity(dto.getQuantity());
            cart.setChecked(1);
            cartMapper.insert(cart);
        }
    }

    @Override
    public List<CartItemDTO> getCartItems() {
        Long memberId = StpMemberUtil.getLoginIdAsLong();

        List<Cart> carts = cartMapper.selectList(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getMemberId, memberId)
                        .orderByDesc(Cart::getCreateTime)
        );

        return carts.stream().map(cart -> {
            CartItemDTO dto = new CartItemDTO();
            dto.setCartId(cart.getCartId());
            dto.setSkuId(cart.getSkuId());
            dto.setSkuName(cart.getSkuName());
            dto.setSkuPrice(cart.getSkuPrice());
            dto.setSkuPic(cart.getSkuPic());
            dto.setQuantity(cart.getQuantity());
            dto.setChecked(cart.getChecked() == 1);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void updateCart(UpdateCartDTO dto) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();

        Cart cart = cartMapper.selectById(dto.getCartId());
        if (cart == null) {
            throw new IllegalArgumentException("购物车项不存在");
        }

        if (!cart.getMemberId().equals(memberId)) {
            throw new SecurityException("无权操作");
        }

        boolean updated = false;

        if (dto.getQuantity() != null) {
            if (dto.getQuantity() < 1) {
                throw new IllegalArgumentException("数量至少为1");
            }
            cart.setQuantity(dto.getQuantity());
            updated = true;
        }

        if (dto.getChecked() != null) {
            cart.setChecked(dto.getChecked() ? 1 : 0);
            updated = true;
        }

        if (updated) {
            cartMapper.updateById(cart);
        }
    }

    private LambdaQueryWrapper<Cart> buildQueryWrapper(CartQueryDTO query) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        if (query.getMemberId() != null) {
            wrapper.eq(Cart::getMemberId, query.getMemberId());
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(query.getSkuName())) {
            wrapper.like(Cart::getSkuName, query.getSkuName());
        }
        wrapper.orderByDesc(Cart::getUpdateTime);
        return wrapper;
    }

    private Map<Long, String> fallbackFetchUsernames(List<Long> memberIds) {
        Map<Long, String> map = new HashMap<>();
        for (Long memberId : memberIds) {
            try {
                Result<String> result = memberFeignClient.getMemberUsername(memberId);
                if (result.getCode() == 200 && result.getData() != null) {
                    map.put(memberId, result.getData());
                } else {
                    map.put(memberId, "用户-" + memberId);
                }
            } catch (Exception e) {
                map.put(memberId, "用户-" + memberId);
            }
        }
        return map;
    }

    @Override
    @Transactional
    public void deleteCartItem(Long memberId, Long cartId) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            throw new IllegalArgumentException("购物车项不存在");
        }
        if (!cart.getMemberId().equals(memberId)) {
            throw new SecurityException("无权操作此购物车项");
        }

        cartMapper.deleteById(cartId);
    }

    @Override
    @Transactional
    public void deleteCartItems(Long memberId, List<Long> cartIds) {
        if (cartIds == null || cartIds.isEmpty()) {
            return;
        }

        List<Cart> carts = cartMapper.selectBatchIds(cartIds);
        for (Cart cart : carts) {
            if (!cart.getMemberId().equals(memberId)) {
                throw new SecurityException("无权操作购物车项: " + cart.getCartId());
            }
        }

        cartMapper.deleteBatchIds(cartIds);
    }

    @Override
    @Transactional
    public void clearCart(Long memberId) {
        cartMapper.delete(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getMemberId, memberId));
    }

    @Override
    public Page<CartDTO> getCartList(CartQueryDTO query) {
        Page<Cart> page = new Page<>(query.getPageNum(), query.getPageSize());

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Cart> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        if (query.getMemberId() != null) {
            wrapper.eq(Cart::getMemberId, query.getMemberId());
        }
        if (org.springframework.util.StringUtils.hasText(query.getSkuName())) {
            wrapper.like(Cart::getSkuName, query.getSkuName());
        }

        wrapper.orderByDesc(Cart::getUpdateTime);

        IPage<Cart> cartPage = cartMapper.selectPage(page, wrapper);

        List<Long> memberIds = cartPage.getRecords().stream()
                .map(Cart::getMemberId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> usernameMap = fetchUsernames(memberIds);

        return (Page<CartDTO>) cartPage.convert(cart -> {
            CartDTO dto = convertToDTO(cart);
            dto.setMemberUsername(usernameMap.getOrDefault(cart.getMemberId(), "未知用户"));
            return dto;
        });
    }

    @Override
    public void batchDeleteCarts(List<Long> cartIds) {
        if (cartIds == null || cartIds.isEmpty()) {
            return;
        }

        this.removeBatchByIds(cartIds);
    }

    @Override
    public void clearUserCart(Long memberId) {
        if (memberId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        this.lambdaUpdate()
                .eq(Cart::getMemberId, memberId)
                .remove();
    }

    private CartDTO convertToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setCartId(cart.getCartId());
        dto.setMemberId(cart.getMemberId());
        dto.setSkuId(cart.getSkuId());
        dto.setSkuName(cart.getSkuName());
        dto.setSkuPrice(cart.getSkuPrice());
        dto.setSkuPic(cart.getSkuPic());
        dto.setQuantity(cart.getQuantity());
        dto.setChecked(cart.getChecked());
        dto.setCreateTime(cart.getCreateTime());
        dto.setUpdateTime(cart.getUpdateTime());
        return dto;
    }

    private Map<Long, String> fetchUsernames(List<Long> memberIds) {
        if (memberIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        try {
            Result<java.util.Map<Long, String>> result = memberFeignClient.getMemberUsernames(memberIds);
            if (result.getData() != null) {
                return result.getData();
            }
        } catch (Exception ignored) {
        }

        return memberIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> "用户-" + id
                ));
    }
}
