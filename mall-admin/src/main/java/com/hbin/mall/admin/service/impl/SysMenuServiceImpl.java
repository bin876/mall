package com.hbin.mall.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.admin.domain.SysMenu;
import com.hbin.mall.admin.domain.SysPermission;
import com.hbin.mall.admin.domain.SysRolePermission;
import com.hbin.mall.admin.domain.SysUserRole;
import com.hbin.mall.admin.dto.*;
import com.hbin.mall.admin.mapper.SysMenuMapper;
import com.hbin.mall.admin.mapper.SysPermissionMapper;
import com.hbin.mall.admin.mapper.SysRolePermissionMapper;
import com.hbin.mall.admin.mapper.SysUserRoleMapper;
import com.hbin.mall.admin.service.SysMenuService;
import com.hbin.mall.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysMenuMapper menuMapper;
    
    @Autowired
    private SysUserRoleMapper userRoleMapper;
    
    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;
    
    @Autowired
    private SysPermissionMapper permissionMapper;

    @Override
    public IPage<SysMenuDto> getMenuList(MenuQueryDTO query) {
        Page<SysMenu> page = new Page<>(query.getPageNum(), query.getPageSize());

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMenu> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getTitle())) {
            wrapper.like(SysMenu::getTitle, query.getTitle());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysMenu::getStatus, query.getStatus());
        }

        wrapper.orderByAsc(SysMenu::getSort)
                .orderByDesc(SysMenu::getMenuId);

        IPage<SysMenu> menuPage = menuMapper.selectPage(page, wrapper);

        return menuPage.convert(this::convertToDto);
    }

    @Override
    public List<SysMenuDto> getMenuTreeByUserId(Long userId) {
        Set<String> permissionCodes = getUserPermissionCodes(userId);

        List<SysMenu> allMenus = menuMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getStatus, 1)
        );

        List<SysMenu> validMenus = new ArrayList<>();

        for (SysMenu menu : allMenus) {
            if (menu.getType() == 2) {
                // 按钮类型需要权限标识
                if (permissionCodes.contains(menu.getPermissionCode())) {
                    validMenus.add(menu);
                }
            } else {
                // 目录/菜单类型，如果设置了权限标识则需要验证，否则直接显示
                if (menu.getPermissionCode() == null ||
                        permissionCodes.contains(menu.getPermissionCode())) {
                    validMenus.add(menu);
                }
            }
        }

        List<SysMenu> menuNodes = validMenus.stream()
                .filter(m -> m.getType() != 2)
                .collect(Collectors.toList());

        return buildMenuTree(menuNodes);
    }

    @Override
    public Set<String> getUserPermissionCodes(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserRole>()
                                .eq(SysUserRole::getUserId, userId)
                ).stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return Collections.emptySet();
        }

        List<Long> permissionIds = rolePermissionMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRolePermission>()
                                .in(SysRolePermission::getRoleId, roleIds)
                ).stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList());

        if (permissionIds.isEmpty()) {
            return Collections.emptySet();
        }

        return permissionMapper.selectBatchIds(permissionIds)
                .stream()
                .map(SysPermission::getPermissionCode)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMenu(SysMenuCreateDTO dto) {
        validateMenu(dto, null);

        SysMenu menu = new SysMenu();
        menu.setParentId(dto.getParentId());
        menu.setTitle(dto.getTitle());
        menu.setName(dto.getName());
        menu.setPath(dto.getPath());
        menu.setComponent(dto.getComponent());
        menu.setPermissionCode(StringUtils.hasText(dto.getPermissionCode()) ? dto.getPermissionCode() : null);
        menu.setIcon(dto.getIcon());
        menu.setType(dto.getType());
        menu.setSort(dto.getSort());
        menu.setHidden(dto.getHidden());
        menu.setStatus(1);

        this.save(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(SysMenuUpdateDTO dto) {
        SysMenu existing = this.getById(dto.getMenuId());
        if (existing == null) {
            throw new BusinessException("菜单不存在");
        }

        validateMenu(dto, dto.getMenuId());

        existing.setTitle(dto.getTitle());
        existing.setName(dto.getName());
        existing.setPath(dto.getPath());
        existing.setComponent(dto.getComponent());
        existing.setPermissionCode(StringUtils.hasText(dto.getPermissionCode()) ? dto.getPermissionCode() : null);
        existing.setIcon(dto.getIcon());
        existing.setType(dto.getType());
        existing.setSort(dto.getSort());
        existing.setHidden(dto.getHidden());
        existing.setStatus(dto.getStatus());

        this.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long menuId) {
        SysMenu menu = this.getById(menuId);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }

        // 检查是否有子菜单
        long childCount = this.lambdaQuery()
                .eq(SysMenu::getParentId, menuId)
                .count();

        if (childCount > 0) {
            throw new BusinessException("请先删除子菜单");
        }

        this.removeById(menuId);
    }

    /**
     * 转换为DTO
     */
    private SysMenuDto convertToDto(SysMenu menu) {
        SysMenuDto dto = new SysMenuDto();
        dto.setMenuId(menu.getMenuId());
        dto.setParentId(menu.getParentId());
        dto.setTitle(menu.getTitle());
        dto.setName(menu.getName());
        dto.setPath(menu.getPath());
        dto.setComponent(menu.getComponent());
        dto.setPermissionCode(menu.getPermissionCode());
        dto.setIcon(menu.getIcon());
        dto.setType(menu.getType());
        dto.setSort(menu.getSort());
        dto.setHidden(menu.getHidden());
        dto.setStatus(menu.getStatus());
        dto.setCreateTime(menu.getCreateTime());
        dto.setUpdateTime(menu.getUpdateTime());
        return dto;
    }

    /**
     * 构建菜单树
     */
    private List<SysMenuDto> buildMenuTree(List<SysMenu> menus) {
        Map<Long, SysMenuDto> map = new HashMap<>();

        for (SysMenu menu : menus) {
            map.put(menu.getMenuId(), convertToDto(menu));
        }

        List<SysMenuDto> roots = new ArrayList<>();
        for (SysMenuDto node : map.values()) {
            SysMenuDto parent = map.get(node.getParentId());
            if (parent != null) {
                parent.getChildren().add(node);
            } else {
                roots.add(node);
            }
        }

        // 排序
        roots.forEach(this::sortChildren);
        return roots;
    }

    /**
     * 递归排序子菜单
     */
    private void sortChildren(SysMenuDto node) {
        node.getChildren().sort(Comparator.comparingInt(m -> m.getSort() != null ? m.getSort() : 0));
        node.getChildren().forEach(this::sortChildren);
    }

    /**
     * 菜单验证
     */
    private void validateMenu(SysMenuBaseDTO dto, Long menuId) {
        // 验证父菜单
        if (dto.getParentId() != 0) {
            boolean parentExists = this.lambdaQuery()
                    .eq(SysMenu::getMenuId, dto.getParentId())
                    .eq(SysMenu::getStatus, 1)
                    .exists();
            if (!parentExists) {
                throw new BusinessException("父菜单不存在或已禁用");
            }

            // 检查循环引用
            if (menuId != null && isChild(menuId, dto.getParentId())) {
                throw new BusinessException("不能设置为自己的子菜单");
            }
        }

        // 验证权限标识（非目录类型）
        if (dto.getType() != 0 && StringUtils.hasText(dto.getPermissionCode())) {
            boolean exists = permissionMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysPermission>()
                            .eq(SysPermission::getPermissionCode, dto.getPermissionCode())
                            .eq(SysPermission::getStatus, 1)
            ) > 0;
            if (!exists) {
                throw new BusinessException("权限标识不存在或已禁用");
            }
        }

        // 验证必填字段
        if (dto.getType() != 0) {
            if (!StringUtils.hasText(dto.getName())) {
                throw new BusinessException("路由名称不能为空");
            }
            if (!StringUtils.hasText(dto.getPath())) {
                throw new BusinessException("路由路径不能为空");
            }
        }

        if (dto.getType() == 1 && !StringUtils.hasText(dto.getComponent())) {
            throw new BusinessException("组件路径不能为空");
        }
    }

    /**
     * 检查是否为子菜单（防止循环引用）
     */
    private boolean isChild(Long menuId, Long parentId) {
        if (parentId == null || parentId == 0) {
            return false;
        }
        if (menuId.equals(parentId)) {
            return true;
        }
        SysMenu parent = this.getById(parentId);
        if (parent == null) {
            return false;
        }
        return isChild(menuId, parent.getParentId());
    }
}
