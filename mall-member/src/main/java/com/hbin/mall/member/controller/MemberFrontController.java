package com.hbin.mall.member.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.hbin.mall.api.file.feign.FileFeignClient;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.common.satoken.StpMemberUtil;
import com.hbin.mall.member.dto.MemberInfoDTO;
import com.hbin.mall.member.dto.RegisterMemberDTO;
import com.hbin.mall.member.dto.UpdateMemberInfoDTO;
import com.hbin.mall.member.dto.UpdatePasswordDTO;
import com.hbin.mall.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/member")
public class MemberFrontController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private FileFeignClient fileFeignClient;

    @GetMapping("/info")
    public Result<MemberInfoDTO> getMemberInfo() {
        Long loginId = StpMemberUtil.getLoginIdAsLong();

        MemberInfoDTO info = memberService.getMemberInfo(loginId);

        if (info == null) {
            return Result.error("用户信息不存在");
        }
        
        return Result.success(info);
    }

    @PutMapping("/info")
    public Result<String> updateMemberInfo(@Valid @RequestBody UpdateMemberInfoDTO dto) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        memberService.updateMemberInfo(memberId, dto);
        return Result.success("会员信息更新成功");
    }

    @PutMapping("/password")
    public Result<String> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        memberService.updatePassword(memberId, dto);
        return Result.success("密码修改成功");
    }

    @PostMapping("/register")
    public Result<String> registerMember(@Valid @RequestBody RegisterMemberDTO dto) {
        memberService.registerMember(dto);
        return Result.success("注册成功");
    }

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestPart MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        return fileFeignClient.upload(file, "avatar");
    }

}