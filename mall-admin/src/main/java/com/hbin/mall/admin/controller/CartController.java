package com.hbin.mall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.order.dto.CartDTO;
import com.hbin.mall.api.order.dto.CartQueryDTO;
import com.hbin.mall.api.order.feign.CartFeignClient;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/cart")
public class CartController {

    @Autowired
    private CartFeignClient cartFeignClient;

    @PostMapping("/list")
    public Result<Page<CartDTO>> getCartList(@Valid @RequestBody CartQueryDTO query) {
        return cartFeignClient.getCartList(query);
    }

    @DeleteMapping("/batch-delete")
    public Result<String> batchDeleteCarts(@RequestParam List<Long> cartIds) {
        return cartFeignClient.batchDeleteCarts(cartIds);
    }

    @DeleteMapping("/clear/{memberId}")
    public Result<String> clearUserCart(@NotNull @PathVariable Long memberId) {
        return cartFeignClient.clearUserCart(memberId);
    }
}