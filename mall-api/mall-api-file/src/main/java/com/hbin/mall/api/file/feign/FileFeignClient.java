package com.hbin.mall.api.file.feign;

import com.hbin.mall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "mall-file", contextId = "file")
public interface FileFeignClient {

    @PostMapping(
            value = "/inner/file/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    Result<String> upload(
            @RequestParam("file") @RequestPart MultipartFile file,
            @RequestParam("bizType") String bizType);
}
