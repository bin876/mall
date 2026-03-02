package com.hbin.mall.admin.controller;

import com.hbin.mall.api.file.feign.FileFeignClient;
import com.hbin.mall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin")
public class ProductImageController {

    @Autowired
    private FileFeignClient fileFeignClient;

    @PostMapping("/spu/image")
    public Result<String> uploadSpuImage(@RequestPart MultipartFile file) {
        return fileFeignClient.upload(file, "spu");
    }

    @PostMapping("/sku/image")
    public Result<String> uploadSkuImage(@RequestPart MultipartFile file) {
        return fileFeignClient.upload(file, "sku");
    }
}