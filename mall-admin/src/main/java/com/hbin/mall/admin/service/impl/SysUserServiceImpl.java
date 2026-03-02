package com.hbin.mall.admin.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.admin.domain.SysPermission;
import com.hbin.mall.admin.domain.SysRolePermission;
import com.hbin.mall.admin.domain.SysUser;
import com.hbin.mall.admin.domain.SysUserRole;
import com.hbin.mall.admin.dto.*;
import com.hbin.mall.admin.mapper.SysPermissionMapper;
import com.hbin.mall.admin.mapper.SysRolePermissionMapper;
import com.hbin.mall.admin.mapper.SysUserMapper;
import com.hbin.mall.admin.mapper.SysUserRoleMapper;
import com.hbin.mall.admin.service.SysUserService;
import com.hbin.mall.api.admin.dto.SysUserLoginRequest;
import com.hbin.mall.common.exception.BusinessException;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.common.result.ResultCode;
import com.hbin.mall.common.satoken.StpAdminUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;

    @Autowired
    private SysPermissionMapper permissionMapper;

    @Override
    @Transactional
    public Result<SaTokenInfo> login(SysUserLoginRequest req) {
        SysUser sysUser = userMapper.selectOne(
                Wrappers.lambdaQuery(SysUser.class)
                        .eq(SysUser::getUsername, req.getUsername())
        );

        if (sysUser == null ||
                !passwordEncoder.matches(req.getPassword(), sysUser.getPassword())) {
            return Result.error(ResultCode.LOGIN_FAILED);
        }

        if (sysUser.getStatus() == 0) {
            return Result.error(ResultCode.ACCOUNT_DISABLED);
        }

        List<String> permissionCodes = getPermissionCodesByUserId(sysUser.getUserId());

        storePermissionsToRedis(sysUser.getUserId(), permissionCodes);

        StpAdminUtil.login(sysUser.getUserId());
        return Result.success(StpAdminUtil.getTokenInfo());
    }

    private List<String> getPermissionCodesByUserId(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(
                new QueryWrapper<SysUserRole>().eq("user_id", userId)
        ).stream().map(SysUserRole::getRoleId).toList();

        if (roleIds.isEmpty()) return Collections.emptyList();

        List<Long> permissionIds = rolePermissionMapper.selectList(
                new QueryWrapper<SysRolePermission>().in("role_id", roleIds)
        ).stream().map(SysRolePermission::getPermissionId).toList();

        if (permissionIds.isEmpty()) return Collections.emptyList();

        return permissionMapper.selectBatchIds(permissionIds)
                .stream()
                .map(SysPermission::getPermissionCode)
                .toList();
    }

    private void storePermissionsToRedis(Long userId, List<String> permissions) {
        String key = "admin:permissions:" + userId;
        if (!permissions.isEmpty()) {
            stringRedisTemplate.opsForSet().add(key, permissions.toArray(new String[0]));
        }
    }

    @Override
    public SysUserInfoDTO getSysUserInfo(Long adminId) {
        SysUser user = userMapper.selectById(adminId);
        if (user == null) {
            throw new BusinessException("管理员不存在");
        }

        SysUserInfoDTO dto = new SysUserInfoDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setEmail(user.getEmail());

        return dto;
    }

    @Override
    @Transactional
    public void updateSysUserInfo(Long adminId, SysUserInfoDTO dto) {
        SysUser user = userMapper.selectById(adminId);
        if (user == null) {
            throw new BusinessException("管理员不存在");
        }

        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            boolean exists = userMapper.exists(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getEmail, dto.getEmail())
                            .ne(SysUser::getUserId, adminId)
            );
            if (exists) {
                throw new BusinessException("邮箱已存在");
            }
        }

        user.setNickname(dto.getNickname());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setEmail(dto.getEmail());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(user);
    }

    @Override
    public IPage<SysUserDto> getUserList(UserQueryDTO query) {
        Page<SysUser> page = new Page<>(query.getPageNum(), query.getPageSize());

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getUsername())) {
            wrapper.like(SysUser::getUsername, query.getUsername());
        }
        if (StringUtils.hasText(query.getRealName())) {
            wrapper.like(SysUser::getRealName, query.getRealName());
        }
        if (StringUtils.hasText(query.getPhone())) {
            wrapper.like(SysUser::getPhone, query.getPhone());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(SysUser::getUserId);

        IPage<SysUser> userPage = userMapper.selectPage(page, wrapper);

        return userPage.convert(this::convertToDto);
    }

    @Override
    public List<SysUserDto> getSimpleUserList() {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getStatus, 1); // 只返回启用的用户
        wrapper.orderByAsc(SysUser::getUsername);

        List<SysUser> users = userMapper.selectList(wrapper);
        return users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(SysUserCreateDTO dto) {
        boolean exists = this.lambdaQuery()
                .eq(SysUser::getUsername, dto.getUsername())
                .exists();

        if (exists) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getRealName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender() != null ? dto.getGender() : 0);
        user.setStatus(1);

        this.save(user);

        assignRoles(user.getUserId(), dto.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUserUpdateDTO dto) {
        SysUser existing = this.getById(dto.getUserId());
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }

        existing.setRealName(dto.getRealName());
        existing.setNickname(dto.getNickname());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());
        existing.setGender(dto.getGender());
        existing.setStatus(dto.getStatus());

        this.updateById(existing);

        assignRoles(dto.getUserId(), dto.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        this.removeById(userId);
        userRoleMapper.deleteByUserId(userId);
    }

    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        return userRoleMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    public List<String> getUserPermissionCodes(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(
                        new QueryWrapper<SysUserRole>().eq("user_id", userId)
                ).stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> permissionIds = rolePermissionMapper.selectList(
                        new QueryWrapper<SysRolePermission>().in("role_id", roleIds)
                ).stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList());

        if (permissionIds.isEmpty()) {
            return Collections.emptyList();
        }

        return permissionMapper.selectList(
                        new QueryWrapper<SysPermission>()
                                .in("permission_id", permissionIds)
                                .eq("status", 1)
                ).stream()
                .map(SysPermission::getPermissionCode)
                .distinct()
                .collect(Collectors.toList());
    }

    private SysUserDto convertToDto(SysUser user) {
        SysUserDto dto = new SysUserDto();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setGender(user.getGender());
        dto.setStatus(user.getStatus());
        dto.setCreateTime(user.getCreateTime());
        dto.setUpdateTime(user.getUpdateTime());
        return dto;
    }

    private void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteByUserId(userId);

        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRole> list = roleIds.stream()
                    .map(rid -> {
                        SysUserRole ur = new SysUserRole();
                        ur.setUserId(userId);
                        ur.setRoleId(rid);
                        return ur;
                    })
                    .collect(Collectors.toList());

            userRoleMapper.insertBatchSomeColumn(list);
        }
    }
}
