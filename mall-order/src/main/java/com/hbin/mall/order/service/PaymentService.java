package com.hbin.mall.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.api.order.dto.PaymentDTO;
import com.hbin.mall.api.order.dto.PaymentDetailDTO;
import com.hbin.mall.api.order.dto.PaymentQueryDTO;
import com.hbin.mall.order.domain.Payment;
import com.hbin.mall.order.dto.PaymentResponse;
import com.hbin.mall.order.dto.SubmitPaymentRequest;
import org.springframework.transaction.annotation.Transactional;

public interface PaymentService extends IService<Payment> {


    @Transactional
    PaymentResponse submitPayment(Long memberId, SubmitPaymentRequest request);

    Page<PaymentDTO> getPaymentList(PaymentQueryDTO query);

    PaymentDetailDTO getPaymentDetail(Long id);
}
