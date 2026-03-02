package com.hbin.mall.member.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.api.member.dto.*;
import com.hbin.mall.common.exception.BusinessException;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.common.result.ResultCode;
import com.hbin.mall.common.satoken.StpMemberUtil;
import com.hbin.mall.member.domain.Member;
import com.hbin.mall.member.domain.MemberAddress;
import com.hbin.mall.member.dto.MemberInfoDTO;
import com.hbin.mall.member.dto.RegisterMemberDTO;
import com.hbin.mall.member.dto.UpdateMemberInfoDTO;
import com.hbin.mall.member.dto.UpdatePasswordDTO;
import com.hbin.mall.member.mapper.MemberAddressMapper;
import com.hbin.mall.member.mapper.MemberMapper;
import com.hbin.mall.member.service.MemberService;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private MemberAddressMapper memberAddressMapper;

    @Override
    @Transactional
    public Result<SaTokenInfo> login(MemberLoginRequest req) {
        Member member = memberMapper.selectOne(
                Wrappers.lambdaQuery(Member.class)
                        .eq(Member::getUsername, req.getUsername())
        );

        if (member == null ||
                !passwordEncoder.matches(req.getPassword(), member.getPassword())) {
            return Result.error(ResultCode.LOGIN_FAILED);
        }

        if (member.getStatus() == 0) {
            return Result.error(ResultCode.ACCOUNT_DISABLED);
        }

        StpMemberUtil.login(member.getMemberId());
        return Result.success(StpMemberUtil.getTokenInfo());
    }

    @Override
    @Transactional
    public void registerMember(RegisterMemberDTO dto) {
        if (memberMapper.exists(new LambdaQueryWrapper<Member>().eq(Member::getUsername, dto.getUsername()))) {
            throw new IllegalArgumentException("用户名已存在");
        }

        if (StringUtils.isNotBlank(dto.getPhone())) {
            if (memberMapper.exists(new LambdaQueryWrapper<Member>().eq(Member::getPhone, dto.getPhone()))) {
                throw new IllegalArgumentException("手机号已存在");
            }
        }

        if (StringUtils.isNotBlank(dto.getEmail())) {
            if (memberMapper.exists(new LambdaQueryWrapper<Member>().eq(Member::getEmail, dto.getEmail()))) {
                throw new IllegalArgumentException("邮箱已存在");
            }
        }

        Member member = new Member();
        member.setUsername(dto.getUsername());
        member.setPassword(passwordEncoder.encode(dto.getPassword())); // 密码加密
        member.setPhone(dto.getPhone());
        member.setEmail(dto.getEmail());
        member.setNickname(dto.getNickname());
        member.setGender(0); // 默认未知
        member.setStatus(1); // 默认启用
        member.setCreateTime(LocalDateTime.now());
        member.setUpdateTime(LocalDateTime.now());

        memberMapper.insert(member);
    }

    @Override
    public String getUsernameById(Long memberId) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<Member>()
                .select(Member::getUsername)
                .eq(Member::getMemberId, memberId);

        Member member = memberMapper.selectOne(wrapper);
        return member != null ? member.getUsername() : null;
    }

    @Override
    public Map<Long, String> getUsernamesByIds(List<Long> memberIds) {
        List<Member> members = memberMapper.selectBatchIds(memberIds);
        return members.stream()
                .collect(Collectors.toMap(Member::getMemberId, Member::getUsername, (a, b) -> a));
    }

    @Override
    public MemberInfoDTO getMemberInfo(Long memberId) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            return null;
        }

        MemberInfoDTO dto = new MemberInfoDTO();
        dto.setMemberId(member.getMemberId());
        dto.setUsername(member.getUsername());
        dto.setNickname(member.getNickname());
        dto.setPhone(member.getPhone());
        dto.setEmail(member.getEmail());
        dto.setAvatarUrl(member.getAvatarUrl());
        dto.setGender(member.getGender());
        dto.setCreateTime(member.getCreateTime());
        return dto;
    }

    @Override
    @Transactional
    public void updateMemberInfo(Long memberId, UpdateMemberInfoDTO dto) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("会员不存在");
        }

        if (!member.getUsername().equals(dto.getUsername())) {
            if (memberMapper.exists(new LambdaQueryWrapper<Member>().eq(Member::getUsername, dto.getUsername()))) {
                throw new IllegalArgumentException("用户名已存在");
            }
        }

        if (StringUtils.isNotBlank(dto.getPhone()) && !dto.getPhone().equals(member.getPhone())) {
            if (memberMapper.exists(new LambdaQueryWrapper<Member>().eq(Member::getPhone, dto.getPhone()))) {
                throw new IllegalArgumentException("手机号已存在");
            }
        }

        if (StringUtils.isNotBlank(dto.getEmail()) && !dto.getEmail().equals(member.getEmail())) {
            if (memberMapper.exists(new LambdaQueryWrapper<Member>().eq(Member::getEmail, dto.getEmail()))) {
                throw new IllegalArgumentException("邮箱已存在");
            }
        }

        member.setUsername(dto.getUsername());
        member.setPhone(dto.getPhone());
        member.setEmail(dto.getEmail());
        member.setNickname(dto.getNickname());
        member.setAvatarUrl(dto.getAvatarUrl());
        member.setGender(dto.getGender());
        memberMapper.updateById(member);
    }

    @Override
    @Transactional
    public void updatePassword(Long memberId, UpdatePasswordDTO dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("新密码与确认密码不一致");
        }

        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("会员不存在");
        }

        if (!passwordEncoder.matches(dto.getOldPassword(), member.getPassword())) {
            throw new IllegalArgumentException("原密码错误");
        }

        member.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        memberMapper.updateById(member);

        StpMemberUtil.logout();
    }

    @Override
    public Page<MemberDTO> getMemberList(MemberQueryDTO query) {
        Page<Member> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();

        if (org.springframework.util.StringUtils.hasText(query.getUsername())) {
            wrapper.like(Member::getUsername, query.getUsername());
        }
        if (org.springframework.util.StringUtils.hasText(query.getNickname())) {
            wrapper.like(Member::getNickname, query.getNickname());
        }
        if (org.springframework.util.StringUtils.hasText(query.getPhone())) {
            wrapper.eq(Member::getPhone, query.getPhone());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Member::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(Member::getCreateTime);

        IPage<Member> memberPage = memberMapper.selectPage(page, wrapper);

        return (Page<MemberDTO>) memberPage.convert(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberDTO getMemberDetail(Long id) {
        Member member = this.getById(id);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        return convertToDTO(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMember(Long id, UpdateMemberDTO dto) {
        Member existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException("会员不存在");
        }

        boolean usernameExists = this.lambdaQuery()
                .eq(Member::getUsername, dto.getUsername())
                .ne(Member::getMemberId, id)
                .exists();
        if (usernameExists) {
            throw new BusinessException("用户名已存在");
        }

        if (org.springframework.util.StringUtils.hasText(dto.getPhone())) {
            boolean phoneExists = this.lambdaQuery()
                    .eq(Member::getPhone, dto.getPhone())
                    .ne(Member::getMemberId, id)
                    .exists();
            if (phoneExists) {
                throw new BusinessException("手机号已存在");
            }
        }

        if (org.springframework.util.StringUtils.hasText(dto.getEmail())) {
            boolean emailExists = this.lambdaQuery()
                    .eq(Member::getEmail, dto.getEmail())
                    .ne(Member::getMemberId, id)
                    .exists();
            if (emailExists) {
                throw new BusinessException("邮箱已存在");
            }
        }

        existing.setUsername(dto.getUsername());
        existing.setNickname(dto.getNickname());
        existing.setPhone(dto.getPhone());
        existing.setEmail(dto.getEmail());
        existing.setGender(dto.getGender());
        existing.setUpdateTime(java.time.LocalDateTime.now());

        this.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMemberStatus(Long id, Integer status) {
        Member member = this.getById(id);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        member.setStatus(status);
        member.setUpdateTime(java.time.LocalDateTime.now());
        this.updateById(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, String newPassword) {
        Member member = this.getById(id);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        member.setPassword(passwordEncoder.encode(newPassword));
        member.setUpdateTime(java.time.LocalDateTime.now());
        this.updateById(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMember(Long id) {
        Member member = this.getById(id);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        this.removeById(id);
    }

    private MemberDTO convertToDTO(Member member) {
        MemberDTO dto = new MemberDTO();
        dto.setMemberId(member.getMemberId());
        dto.setUsername(member.getUsername());
        dto.setNickname(member.getNickname());
        dto.setPhone(member.getPhone());
        dto.setEmail(member.getEmail());
        dto.setAvatarUrl(member.getAvatarUrl());
        dto.setGender(member.getGender());
        dto.setGenderDesc(getGenderDesc(member.getGender()));
        dto.setStatus(member.getStatus());
        dto.setStatusDesc(getStatusDesc(member.getStatus()));
        dto.setCreateTime(member.getCreateTime());
        dto.setUpdateTime(member.getUpdateTime());
        return dto;
    }

    private String getGenderDesc(Integer gender) {
        if (gender == null) return "未知";
        switch (gender) {
            case 0: return "未知";
            case 1: return "男";
            case 2: return "女";
            default: return "未知";
        }
    }

    private String getStatusDesc(Integer status) {
        if (status == null) return "未知";
        return status == 1 ? "启用" : "禁用";
    }

    @Override
    public Map<Long, String> getMemberAvatarUrls(List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> validIds = memberIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .distinct()
                .collect(Collectors.toList());

        if (validIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Member> members = this.listByIds(validIds);

        Map<Long, String> result = new HashMap<>();

        for (Long id : validIds) {
            result.put(id, "/default-avatar.png");
        }

        for (Member member : members) {
            if (member.getAvatarUrl() != null && !member.getAvatarUrl().trim().isEmpty()) {
                result.put(member.getMemberId(), member.getAvatarUrl());
            } else {
                result.put(member.getMemberId(), "/default-avatar.png");
            }
        }

        return result;
    }

    @Override
    public Result<MemberAddressDTO> getDefaultAddress(Long memberId) {
        if (memberId == null) {
            return Result.error("用户ID不能为空");
        }

        try {
            LambdaQueryWrapper<MemberAddress> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MemberAddress::getMemberId, memberId)
                    .eq(MemberAddress::getIsDefault, 1)
                    .last("LIMIT 1");

            MemberAddress address = memberAddressMapper.selectOne(wrapper);

            if (address == null) {
                LambdaQueryWrapper<MemberAddress> firstWrapper = new LambdaQueryWrapper<>();
                firstWrapper.eq(MemberAddress::getMemberId, memberId)
                        .orderByAsc(MemberAddress::getCreateTime)
                        .last("LIMIT 1");

                address = memberAddressMapper.selectOne(firstWrapper);

                if (address == null) {
                    return Result.error("用户未设置收货地址");
                }

                address.setIsDefault(1);
                memberAddressMapper.updateById(address);
            }

            MemberAddressDTO dto = convertToDTO(address);
            return Result.success(dto);

        } catch (Exception e) {
            return Result.error("获取地址信息失败，请稍后重试");
        }
    }

    private MemberAddressDTO convertToDTO(MemberAddress address) {
        MemberAddressDTO dto = new MemberAddressDTO();
        dto.setAddressId(address.getAddressId());
        dto.setMemberId(address.getMemberId());
        dto.setReceiverName(address.getReceiverName());
        dto.setReceiverPhone(address.getReceiverPhone());
        dto.setProvince(address.getReceiverProvince());
        dto.setCity(address.getReceiverCity());
        dto.setRegion(address.getReceiverRegion());
        dto.setDetailAddress(address.getReceiverDetailAddress());
        dto.setPostCode(address.getReceiverPostCode());
        dto.setIsDefault(address.getIsDefault() == 1);
        return dto;
    }

}
