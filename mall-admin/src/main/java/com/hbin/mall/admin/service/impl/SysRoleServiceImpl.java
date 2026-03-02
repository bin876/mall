package com.hbin.mall.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.admin.domain.SysRole;
import com.hbin.mall.admin.domain.SysRolePermission;
import com.hbin.mall.admin.domain.SysUserRole;
import com.hbin.mall.admin.dto.RoleQueryDTO;
import com.hbin.mall.admin.dto.SysRoleCreateDTO;
import com.hbin.mall.admin.dto.SysRoleDto;
import com.hbin.mall.admin.dto.SysRoleUpdateDTO;
import com.hbin.mall.admin.mapper.SysRoleMapper;
import com.hbin.mall.admin.mapper.SysRolePermissionMapper;
import com.hbin.mall.admin.mapper.SysUserRoleMapper;
import com.hbin.mall.admin.service.SysRoleService;
import com.hbin.mall.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Override
    public IPage<SysRoleDto> getRoleList(RoleQueryDTO query) {
        Page<SysRole> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getRoleCode())) {
            wrapper.like(SysRole::getRoleCode, query.getRoleCode());
        }
        if (StringUtils.hasText(query.getRoleName())) {
            wrapper.like(SysRole::getRoleName, query.getRoleName());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysRole::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(SysRole::getRoleId);

        IPage<SysRole> rolePage = roleMapper.selectPage(page, wrapper);

        return rolePage.convert(this::convertToDtoWithUsage);
    }

    @Override
    public List<SysRoleDto> getSimpleRoleList() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getStatus, 1);
        wrapper.orderByAsc(SysRole::getRoleCode);

        List<SysRole> roles = roleMapper.selectList(wrapper);
        return roles.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRole(SysRoleCreateDTO dto) {
        boolean exists = this.lambdaQuery()
                .eq(SysRole::getRoleCode, dto.getRoleCode())
                .exists();

        if (exists) {
            throw new BusinessException("角色编码已存在");
        }

        SysRole role = new SysRole();
        role.setRoleCode(dto.getRoleCode());
        role.setRoleName(dto.getRoleName());
        role.setStatus(1);

        this.save(role);

        // 分配权限
        assignPermissions(role.getRoleId(), dto.getPermissionIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(SysRoleUpdateDTO dto) {
        SysRole existing = this.getById(dto.getRoleId());
        if (existing == null) {
            throw new BusinessException("角色不存在");
        }

        if (!existing.getRoleCode().equals(dto.getRoleCode())) {
            boolean codeExists = this.lambdaQuery()
                    .ne(SysRole::getRoleId, dto.getRoleId())
                    .eq(SysRole::getRoleCode, dto.getRoleCode())
                    .exists();

            if (codeExists) {
                throw new BusinessException("角色编码已存在");
            }
        }

        existing.setRoleCode(dto.getRoleCode());
        existing.setRoleName(dto.getRoleName());
        existing.setStatus(dto.getStatus());

        this.updateById(existing);

        // 更新权限
        assignPermissions(dto.getRoleId(), dto.getPermissionIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        SysRole role = this.getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        long userCount = userRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, roleId)
        );

        if (userCount > 0) {
            throw new BusinessException("该角色已被用户使用，无法删除");
        }

        this.removeById(roleId);
        rolePermissionMapper.deleteByRoleId(roleId);
    }

    @Override
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        return rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }

    private SysRoleDto convertToDtoWithUsage(SysRole role) {
        SysRoleDto dto = convertToDto(role);

        Long usedByUsers = userRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, role.getRoleId())
        );

        dto.setUsedByUsers(usedByUsers);
        dto.setCanDelete(usedByUsers == 0);

        return dto;
    }

    private SysRoleDto convertToDto(SysRole role) {
        SysRoleDto dto = new SysRoleDto();
        dto.setRoleId(role.getRoleId());
        dto.setRoleCode(role.getRoleCode());
        dto.setRoleName(role.getRoleName());
        dto.setStatus(role.getStatus());
        dto.setCreateTime(role.getCreateTime());
        dto.setUpdateTime(role.getUpdateTime());
        return dto;
    }

    private void assignPermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>()
                        .eq(SysRolePermission::getRoleId, roleId)
        );

        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<SysRolePermission> list = permissionIds.stream()
                    .map(pid -> {
                        SysRolePermission rp = new SysRolePermission();
                        rp.setRoleId(roleId);
                        rp.setPermissionId(pid);
                        return rp;
                    })
                    .collect(Collectors.toList());

            rolePermissionMapper.insertBatchSomeColumn(list);
        }
    }
}
