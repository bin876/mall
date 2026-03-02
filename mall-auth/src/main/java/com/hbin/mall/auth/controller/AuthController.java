package com.hbin.mall.auth.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.hbin.mall.api.admin.dto.SysUserLoginRequest;
import com.hbin.mall.api.admin.feign.SysUserApi;
import com.hbin.mall.api.member.dto.MemberLoginRequest;
import com.hbin.mall.api.member.feign.MemberFeignClient;
import com.hbin.mall.auth.dto.LoginRequest;
import com.hbin.mall.common.constant.AuthConstant;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.common.result.ResultCode;
import com.hbin.mall.common.satoken.StpAdminUtil;
import com.hbin.mall.common.satoken.StpMemberUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private SysUserApi sysUserAuthFeignClient;

    @Autowired
    private MemberFeignClient memberAuthFeignClient;

    @PostMapping("/login")
    public Result<SaTokenInfo> login(@RequestBody LoginRequest req) {
        if (AuthConstant.ADMIN_CLIENT_ID.equals(req.getClientId())) {
            SysUserLoginRequest loginRequest = new SysUserLoginRequest();
            loginRequest.setUsername(req.getUsername());
            loginRequest.setPassword(req.getPassword());
            return sysUserAuthFeignClient.login(loginRequest);
        } else if (AuthConstant.MEMBER_CLIENT_ID.equals(req.getClientId())) {
            MemberLoginRequest loginRequest = new MemberLoginRequest();
            loginRequest.setUsername(req.getUsername());
            loginRequest.setPassword(req.getPassword());
            return memberAuthFeignClient.login(loginRequest);
        } else {
            return Result.error(ResultCode.BAD_REQUEST);
        }
    }

    @GetMapping("/logout")
    public Result<String> logout() {
        if(StpMemberUtil.isLogin()) {
            StpMemberUtil.logout();
            return Result.success("退出成功");
        }
        if(StpAdminUtil.isLogin()) {
            StpAdminUtil.logout();
            return Result.success("退出成功");
        }

        return Result.error("未登录");
    }
}
