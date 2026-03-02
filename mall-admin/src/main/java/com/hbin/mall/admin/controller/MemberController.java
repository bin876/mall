package com.hbin.mall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.member.dto.*;
import com.hbin.mall.api.member.feign.MemberFeignClient;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/member")
public class MemberController {

    @Autowired
    private MemberFeignClient memberFeignClient;

    @PostMapping("/list")
    public Result<Page<MemberDTO>> getMemberList(@Valid @RequestBody MemberQueryDTO query) {
        return memberFeignClient.getMemberList(query);
    }

    @GetMapping("/{id}")
    public Result<MemberDTO> getMemberDetail(@NotNull @PathVariable Long id) {
        return memberFeignClient.getMemberDetail(id);
    }

    @PutMapping("/{id}")
    public Result<String> updateMember(
            @NotNull @PathVariable Long id,
            @Valid @RequestBody UpdateMemberDTO dto) {
        return memberFeignClient.updateMember(id, dto);
    }

    @PutMapping("/{id}/status")
    public Result<String> updateMemberStatus(
            @NotNull @PathVariable Long id,
            @Valid @RequestBody UpdateMemberStatusDTO dto) {
        return memberFeignClient.updateMemberStatus(id, dto);
    }

    @PutMapping("/{id}/password")
    public Result<String> resetPassword(
            @NotNull @PathVariable Long id,
            @Valid @RequestBody ResetPasswordDTO dto) {
        return memberFeignClient.resetPassword(id, dto);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteMember(@NotNull @PathVariable Long id) {
        return memberFeignClient.deleteMember(id);
    }
}
