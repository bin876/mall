package com.hbin.mall.admin.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.hbin.mall.admin.service.SysUserService;
import com.hbin.mall.api.admin.dto.SysUserLoginRequest;
import com.hbin.mall.api.admin.feign.SysUserApi;
import com.hbin.mall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SysUserAuthController implements SysUserApi {

    @Autowired
    private SysUserService sysUserService;

    @Override
    public Result<SaTokenInfo> login(@RequestBody SysUserLoginRequest req) {
        return sysUserService.login(req);
    }
}
