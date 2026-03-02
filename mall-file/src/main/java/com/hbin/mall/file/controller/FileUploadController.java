package com.hbin.mall.file.controller;

import com.hbin.mall.api.file.feign.FileFeignClient;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.file.service.impl.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileUploadController implements FileFeignClient {

    @Autowired
    private FileUploadService fileUploadService;

    @Override
    public Result<String> upload(
            @RequestParam("file") @RequestPart MultipartFile file,
            @RequestParam("bizType") String bizType) {

        String url;
        try {
            url = fileUploadService.upload(file, bizType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Result.success(url);
    }
}