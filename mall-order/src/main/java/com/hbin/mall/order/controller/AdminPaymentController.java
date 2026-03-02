package com.hbin.mall.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.order.dto.PaymentDTO;
import com.hbin.mall.api.order.dto.PaymentDetailDTO;
import com.hbin.mall.api.order.dto.PaymentQueryDTO;
import com.hbin.mall.api.order.feign.PaymentFeignClient;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.order.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdminPaymentController implements PaymentFeignClient {

    @Autowired
    private PaymentService paymentService;

    @Override
    public Result<Page<PaymentDTO>> getPaymentList(@Valid @RequestBody PaymentQueryDTO query) {
        Page<PaymentDTO> result = paymentService.getPaymentList(query);
        return Result.success(result);
    }

    @Override
    public Result<PaymentDetailDTO> getPaymentDetail(@NotNull @PathVariable Long id) {
        PaymentDetailDTO payment = paymentService.getPaymentDetail(id);
        return Result.success(payment);
    }
}