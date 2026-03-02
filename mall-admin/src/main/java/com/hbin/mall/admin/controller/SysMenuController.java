package com.hbin.mall.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hbin.mall.admin.dto.*;
import com.hbin.mall.admin.service.SysMenuService;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.common.satoken.StpAdminUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/menu")
public class SysMenuController {

    @Autowired
    private SysMenuService menuService;

    @PostMapping("/list")
    public Result<IPage<SysMenuDto>> list(@Valid @RequestBody MenuQueryDTO query) {
        IPage<SysMenuDto> result = menuService.getMenuList(query);
        return Result.success(result);
    }

    /**
     * 获取菜单树
     */
    @GetMapping("/tree")
    public Result<List<SysMenuDto>> getMenuTree() {
        Long userId = StpAdminUtil.getLoginIdAsLong();
        List<SysMenuDto> tree = menuService.getMenuTreeByUserId(userId);
        return Result.success(tree);
    }

    /**
     * 获取用户权限标识
     */
    @GetMapping("/permissions")
    public Result<Set<String>> getPermissions() {
        Long userId = StpAdminUtil.getLoginIdAsLong();
        Set<String> permissions = menuService.getUserPermissionCodes(userId);
        return Result.success(permissions);
    }

    /**
     * 创建菜单
     */
    @PostMapping
    public Result<String> create(@Valid @RequestBody SysMenuCreateDTO dto) {
        menuService.createMenu(dto);
        return Result.success("菜单创建成功");
    }

    /**
     * 更新菜单
     */
    @PutMapping
    public Result<String> update(@Valid @RequestBody SysMenuUpdateDTO dto) {
        menuService.updateMenu(dto);
        return Result.success("菜单更新成功");
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@NotNull @PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.success("菜单删除成功");
    }
}