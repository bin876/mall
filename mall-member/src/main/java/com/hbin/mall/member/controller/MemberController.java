package com.hbin.mall.member.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.member.dto.*;
import com.hbin.mall.api.member.feign.MemberFeignClient;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.member.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class MemberController implements MemberFeignClient {

    @Autowired
    private MemberService memberService;

    @Override
    public Result<SaTokenInfo> login(MemberLoginRequest req) {
        return memberService.login(req);
    }

    @Override
    public Result<String> getMemberUsername(@PathVariable Long memberId) {
        if (memberId == null || memberId <= 0) {
            return Result.error("无效的会员ID");
        }

        String username = memberService.getUsernameById(memberId);
        if (username == null) {
            return Result.error("用户不存在");
        }
        return Result.success(username);
    }

    @Override
    public Result<Map<Long, String>> getMemberUsernames(@RequestBody List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Result.success(Collections.emptyMap());
        }
        Map<Long, String> map = memberService.getUsernamesByIds(memberIds);
        return Result.success(map);
    }

    @Override
    public Result<Page<MemberDTO>> getMemberList(@Valid @RequestBody MemberQueryDTO query) {
        Page<MemberDTO> result = memberService.getMemberList(query);
        return Result.success(result);
    }

    @Override
    public Result<MemberDTO> getMemberDetail(@NotNull @PathVariable Long id) {
        MemberDTO member = memberService.getMemberDetail(id);
        return Result.success(member);
    }

    @Override
    public Result<String> updateMember(
            @NotNull @PathVariable Long id,
            @Valid @RequestBody UpdateMemberDTO dto) {
        memberService.updateMember(id, dto);
        return Result.success("会员信息更新成功");
    }

    @Override
    public Result<String> updateMemberStatus(
            @NotNull @PathVariable Long id,
            @Valid @RequestBody UpdateMemberStatusDTO dto) {
        memberService.updateMemberStatus(id, dto.getStatus());
        return Result.success("会员状态更新成功");
    }

    @Override
    public Result<String> resetPassword(
            @NotNull @PathVariable Long id,
            @Valid @RequestBody ResetPasswordDTO dto) {
        memberService.resetPassword(id, dto.getNewPassword());
        return Result.success("密码重置成功");
    }

    @Override
    public Result<String> deleteMember(@NotNull @PathVariable Long id) {
        memberService.deleteMember(id);
        return Result.success("会员删除成功");
    }

    @Override
    public Result<Map<Long, String>> getMemberAvatarUrls(@RequestBody List<Long> memberIds) {
        try {
            Map<Long, String> avatarUrls = memberService.getMemberAvatarUrls(memberIds);
            return Result.success(avatarUrls);
        } catch (Exception e) {
            return Result.error("获取用户头像失败");
        }
    }

    @Override
    public Result<MemberAddressDTO> getDefaultAddress(Long memberId) {
        return memberService.getDefaultAddress(memberId);
    }
}
