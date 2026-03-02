package com.hbin.mall.api.admin.feign;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.hbin.mall.api.admin.dto.SysUserLoginRequest;
import com.hbin.mall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mall-admin")
public interface SysUserApi {
    
    @PostMapping("/inner/login")
    Result<SaTokenInfo> login(@RequestBody SysUserLoginRequest req);
}