package com.hbin.mall.order.controller;

import com.hbin.mall.common.result.Result;
import com.hbin.mall.common.satoken.StpMemberUtil;
import com.hbin.mall.order.dto.AddCartDTO;
import com.hbin.mall.order.dto.CartItemDTO;
import com.hbin.mall.order.dto.DeleteCartItemsRequest;
import com.hbin.mall.order.dto.UpdateCartDTO;
import com.hbin.mall.order.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public Result<String> addToCart(@Valid @RequestBody AddCartDTO dto) {
        cartService.addToCart(dto);
        return Result.success("添加成功");
    }

    @PutMapping("/update")
    public Result<String> updateCart(@RequestBody UpdateCartDTO dto) {
        cartService.updateCart(dto);
        return Result.success("更新成功");
    }

    @GetMapping("/items")
    public Result<List<CartItemDTO>> getCartItems() {
        List<CartItemDTO> items = cartService.getCartItems();
        return Result.success(items);
    }

    /**
     * 删除单个购物车项
     */
    @DeleteMapping("/{cartId}")
    public Result<String> deleteCartItem(@PathVariable Long cartId) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        cartService.deleteCartItem(memberId, cartId);
        return Result.success("删除成功");
    }

    /**
     * 批量删除购物车项
     */
    @PostMapping("/delete/batch")
    public Result<String> deleteCartItems(@RequestBody DeleteCartItemsRequest request) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        cartService.deleteCartItems(memberId, request.getCartIds());
        return Result.success("删除成功");
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    public Result<String> clearCart() {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        cartService.clearCart(memberId);
        return Result.success("购物车已清空");
    }
}