package com.hbin.mall.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.api.order.dto.CartDTO;
import com.hbin.mall.api.order.dto.CartQueryDTO;
import com.hbin.mall.order.domain.Cart;
import com.hbin.mall.order.dto.AddCartDTO;
import com.hbin.mall.order.dto.CartItemDTO;
import com.hbin.mall.order.dto.UpdateCartDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CartService extends IService<Cart> {

    @Transactional
    void addToCart(AddCartDTO dto);

    List<CartItemDTO> getCartItems();

    void updateCart(UpdateCartDTO dto);

    void deleteCartItem(Long memberId, Long cartId);

    @Transactional
    void deleteCartItems(Long memberId, List<Long> cartIds);

    @Transactional
    void clearCart(Long memberId);

    Page<CartDTO> getCartList(CartQueryDTO query);

    void batchDeleteCarts(List<Long> cartIds);

    void clearUserCart(Long memberId);
}
