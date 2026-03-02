package com.hbin.mall.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.admin.domain.SysMenu;
import com.hbin.mall.admin.domain.SysPermission;
import com.hbin.mall.admin.domain.SysRolePermission;
import com.hbin.mall.admin.dto.PermissionQueryDTO;
import com.hbin.mall.admin.dto.SysPermissionCreateDTO;
import com.hbin.mall.admin.dto.SysPermissionDto;
import com.hbin.mall.admin.dto.SysPermissionUpdateDTO;
import com.hbin.mall.admin.mapper.SysMenuMapper;
import com.hbin.mall.admin.mapper.SysPermissionMapper;
import com.hbin.mall.admin.mapper.SysRolePermissionMapper;
import com.hbin.mall.admin.service.SysPermissionService;
import com.hbin.mall.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {

    @Autowired
    private SysPermissionMapper permissionMapper;

    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;

    @Autowired
    private SysMenuMapper menuMapper;

    @Override
    public IPage<SysPermissionDto> getPermissionList(PermissionQueryDTO query) {
        Page<SysPermission> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<SysPermission> wrapper =
                new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getPermissionCode())) {
            wrapper.like(SysPermission::getPermissionCode, query.getPermissionCode());
        }
        if (StringUtils.hasText(query.getPermissionName())) {
            wrapper.like(SysPermission::getPermissionName, query.getPermissionName());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysPermission::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(SysPermission::getPermissionId);

        IPage<SysPermission> permissionPage = permissionMapper.selectPage(page, wrapper);

        return permissionPage.convert(this::convertToDtoWithUsage);
    }

    @Override
    public List<SysPermissionDto> getSimplePermissionList() {
        LambdaQueryWrapper<SysPermission> wrapper =
                new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getStatus, 1);
        wrapper.orderByAsc(SysPermission::getPermissionCode);

        List<SysPermission> permissions = permissionMapper.selectList(wrapper);

        return permissions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPermission(SysPermissionCreateDTO dto) {
        boolean exists = this.lambdaQuery()
                .eq(SysPermission::getPermissionCode, dto.getPermissionCode())
                .exists();

        if (exists) {
            throw new BusinessException("权限标识已存在");
        }

        SysPermission permission = new SysPermission();
        permission.setPermissionCode(dto.getPermissionCode());
        permission.setPermissionName(dto.getPermissionName());
        permission.setStatus(1);

        this.save(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermission(SysPermissionUpdateDTO dto) {
        SysPermission existing = this.getById(dto.getPermissionId());
        if (existing == null) {
            throw new BusinessException("权限不存在");
        }

        if (!existing.getPermissionCode().equals(dto.getPermissionCode())) {
            boolean codeExists = this.lambdaQuery()
                    .ne(SysPermission::getPermissionId, dto.getPermissionId())
                    .eq(SysPermission::getPermissionCode, dto.getPermissionCode())
                    .exists();

            if (codeExists) {
                throw new BusinessException("权限标识已存在");
            }
        }

        existing.setPermissionCode(dto.getPermissionCode());
        existing.setPermissionName(dto.getPermissionName());
        existing.setStatus(dto.getStatus());

        this.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long permissionId) {
        SysPermission permission = this.getById(permissionId);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }

        long roleCount = rolePermissionMapper.selectCount(
                new LambdaQueryWrapper<SysRolePermission>()
                        .eq(SysRolePermission::getPermissionId, permissionId)
        );

        if (roleCount > 0) {
            throw new BusinessException("该权限已被角色使用，无法删除");
        }

        String permissionCode = permission.getPermissionCode();
        if (permissionCode != null) {
            long menuCount = menuMapper.selectCount(
                    new LambdaQueryWrapper<SysMenu>()
                            .eq(SysMenu::getPermissionCode, permissionCode)
            );

            if (menuCount > 0) {
                throw new BusinessException("该权限已被菜单使用，无法删除");
            }
        }

        this.removeById(permissionId);
    }

    private SysPermissionDto convertToDtoWithUsage(SysPermission permission) {
        SysPermissionDto dto = convertToDto(permission);

        Long usedByRoles = rolePermissionMapper.selectCount(
                new LambdaQueryWrapper<SysRolePermission>()
                        .eq(SysRolePermission::getPermissionId, permission.getPermissionId())
        );

        Long usedByMenus = menuMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getPermissionCode, permission.getPermissionCode())
        );

        dto.setUsedByRoles(usedByRoles);
        dto.setUsedByMenus(usedByMenus);
        dto.setCanDelete(usedByRoles == 0 && usedByMenus == 0);

        return dto;
    }

    private SysPermissionDto convertToDto(SysPermission permission) {
        SysPermissionDto dto = new SysPermissionDto();
        dto.setPermissionId(permission.getPermissionId());
        dto.setPermissionCode(permission.getPermissionCode());
        dto.setPermissionName(permission.getPermissionName());
        dto.setStatus(permission.getStatus());
        dto.setCreateTime(permission.getCreateTime());
        dto.setUpdateTime(permission.getUpdateTime());
        return dto;
    }
}
