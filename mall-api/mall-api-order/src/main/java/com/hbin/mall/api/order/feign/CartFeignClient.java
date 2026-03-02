package com.hbin.mall.api.order.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.order.dto.CartDTO;
import com.hbin.mall.api.order.dto.CartQueryDTO;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "mall-order", contextId = "cart")
public interface CartFeignClient {

    @PostMapping("/inner/cart/list")
    Result<Page<CartDTO>> getCartList(@Valid @RequestBody CartQueryDTO query);

    @DeleteMapping("/inner/cart/batch-delete")
    Result<String> batchDeleteCarts(@RequestParam List<Long> cartIds);

    @DeleteMapping("/inner/cart/clear/{memberId}")
    Result<String> clearUserCart(@NotNull @PathVariable Long memberId);
}
