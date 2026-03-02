package com.hbin.mall.order.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.common.satoken.StpMemberUtil;
import com.hbin.mall.order.dto.PaymentResponse;
import com.hbin.mall.order.dto.SubmitPaymentRequest;
import com.hbin.mall.order.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/submit")
    public Result<PaymentResponse> submitPayment(@Valid @RequestBody SubmitPaymentRequest request) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        PaymentResponse response = paymentService.submitPayment(memberId, request);
        return Result.success(response);
    }
}
