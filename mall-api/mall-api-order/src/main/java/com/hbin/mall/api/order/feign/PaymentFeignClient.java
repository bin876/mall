package com.hbin.mall.api.order.feign;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.order.dto.PaymentDTO;
import com.hbin.mall.api.order.dto.PaymentDetailDTO;
import com.hbin.mall.api.order.dto.PaymentQueryDTO;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mall-order", contextId = "payment")
public interface PaymentFeignClient {

    @PostMapping("/inner/payment/list")
    Result<Page<PaymentDTO>> getPaymentList(@Valid @RequestBody PaymentQueryDTO query);

    @GetMapping("/inner/payment/{id}")
    Result<PaymentDetailDTO> getPaymentDetail(@NotNull @PathVariable Long id);
}
