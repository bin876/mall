package com.hbin.mall.admin.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.admin.domain.SysUser;
import com.hbin.mall.admin.dto.*;
import com.hbin.mall.api.admin.dto.SysUserLoginRequest;
import com.hbin.mall.common.result.Result;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SysUserService extends IService<SysUser> {

    Result<SaTokenInfo> login(SysUserLoginRequest req);

    SysUserInfoDTO getSysUserInfo(Long adminId);

    @Transactional
    void updateSysUserInfo(Long adminId, SysUserInfoDTO dto);

    IPage<SysUserDto> getUserList(UserQueryDTO query);

    List<SysUserDto> getSimpleUserList();

    @Transactional
    void createUser(SysUserCreateDTO dto);

    @Transactional
    void updateUser(SysUserUpdateDTO dto);

    @Transactional
    void deleteUser(Long userId);

    List<Long> getRoleIdsByUserId(Long userId);

    List<String> getUserPermissionCodes(Long userId);
}
