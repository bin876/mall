package com.hbin.mall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.order.dto.PaymentDTO;
import com.hbin.mall.api.order.dto.PaymentDetailDTO;
import com.hbin.mall.api.order.dto.PaymentQueryDTO;
import com.hbin.mall.api.order.feign.PaymentFeignClient;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/payment")
public class PaymentController {

    @Autowired
    private PaymentFeignClient paymentFeignClient;

    @PostMapping("/list")
    public Result<Page<PaymentDTO>> getPaymentList(@Valid @RequestBody PaymentQueryDTO query) {
        return  paymentFeignClient.getPaymentList(query);
    }

    @GetMapping("/{id}")
    public Result<PaymentDetailDTO> getPaymentDetail(@NotNull @PathVariable Long id) {
        return paymentFeignClient.getPaymentDetail(id);
    }
}