package com.hbin.mall.member.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.api.member.dto.*;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.member.domain.Member;
import com.hbin.mall.member.dto.MemberInfoDTO;
import com.hbin.mall.member.dto.RegisterMemberDTO;
import com.hbin.mall.member.dto.UpdateMemberInfoDTO;
import com.hbin.mall.member.dto.UpdatePasswordDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface MemberService extends IService<Member> {

    Result<SaTokenInfo> login(MemberLoginRequest req);

    @Transactional
    void registerMember(RegisterMemberDTO dto);

    String getUsernameById(Long memberId);

    Map<Long, String> getUsernamesByIds(List<Long> memberIds);

    MemberInfoDTO getMemberInfo(Long memberId);

    @Transactional
    void updateMemberInfo(Long memberId, UpdateMemberInfoDTO dto);

    // MemberServiceImpl.java
    @Transactional
    void updatePassword(Long memberId, UpdatePasswordDTO dto);

    Page<MemberDTO> getMemberList(MemberQueryDTO query);

    @Transactional(readOnly = true)
    MemberDTO getMemberDetail(Long id);

    @Transactional(rollbackFor = Exception.class)
    void updateMember(Long id, UpdateMemberDTO dto);

    @Transactional(rollbackFor = Exception.class)
    void updateMemberStatus(Long id, Integer status);

    @Transactional(rollbackFor = Exception.class)
    void resetPassword(Long id, String newPassword);

    @Transactional(rollbackFor = Exception.class)
    void deleteMember(Long id);

    Map<Long, String> getMemberAvatarUrls(List<Long> memberIds);

    Result<MemberAddressDTO> getDefaultAddress(Long memberId);
}
