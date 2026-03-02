package com.hbin.mall.admin.controller;

import com.hbin.mall.api.product.dto.PublishSpuRequest;
import com.hbin.mall.api.product.feign.SpuFeignClient;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/spu")
public class ProductController {

    @Autowired
    private SpuFeignClient spuFeignClient;

    @PostMapping
    public Result<String> publishSpu(@Valid @RequestBody PublishSpuRequest request) {
        return spuFeignClient.publishSpu(request);
    }
}
