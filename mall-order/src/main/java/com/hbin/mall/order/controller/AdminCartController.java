package com.hbin.mall.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.order.dto.CartDTO;
import com.hbin.mall.api.order.dto.CartQueryDTO;
import com.hbin.mall.api.order.feign.CartFeignClient;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.order.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AdminCartController implements CartFeignClient {

    @Autowired
    private CartService cartService;

    @Override
    public Result<Page<CartDTO>> getCartList(@Valid @RequestBody CartQueryDTO query) {
        Page<CartDTO> result = cartService.getCartList(query);
        return Result.success(result);
    }

    @Override
    public Result<String> batchDeleteCarts(@RequestParam List<Long> cartIds) {
        cartService.batchDeleteCarts(cartIds);
        return Result.success("批量删除成功");
    }

    @Override
    public Result<String> clearUserCart(@NotNull @PathVariable Long memberId) {
        cartService.clearUserCart(memberId);
        return Result.success("清空购物车成功");
    }
}
