package com.hbin.mall.api.member.feign;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.member.dto.*;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "mall-member", contextId = "member")
public interface MemberFeignClient {
    
    @PostMapping("/inner/member/login")
    Result<SaTokenInfo> login(@RequestBody MemberLoginRequest req);

    @GetMapping("/inner/member/username/{memberId}")
    Result<String> getMemberUsername(@PathVariable Long memberId);

    @PostMapping("/inner/member/usernames")
    Result<Map<Long, String>> getMemberUsernames(@RequestBody List<Long> memberIds);

    @PostMapping("/inner/member/list")
    Result<Page<MemberDTO>> getMemberList(@Valid @RequestBody MemberQueryDTO query);

    @GetMapping("/inner/member/{id}")
    Result<MemberDTO> getMemberDetail(@NotNull @PathVariable Long id);

    @PutMapping("/inner/member/{id}")
    Result<String> updateMember(
            @NotNull @PathVariable Long id,
            @Valid @RequestBody UpdateMemberDTO dto);

    @PutMapping("/inner/member/{id}/status")
    Result<String> updateMemberStatus(
            @NotNull @PathVariable Long id,
            @Valid @RequestBody UpdateMemberStatusDTO dto);

    @PutMapping("/inner/member/{id}/password")
    Result<String> resetPassword(
            @NotNull @PathVariable Long id,
            @Valid @RequestBody ResetPasswordDTO dto);

    @DeleteMapping("/inner/member/{id}")
    Result<String> deleteMember(@NotNull @PathVariable Long id);

    @PostMapping("/inner/member/avatar-urls")
    Result<Map<Long, String>> getMemberAvatarUrls(@RequestBody List<Long> memberIds);

    @GetMapping("/inner/address/default/{memberId}")
    Result<MemberAddressDTO> getDefaultAddress(@PathVariable Long memberId);
}