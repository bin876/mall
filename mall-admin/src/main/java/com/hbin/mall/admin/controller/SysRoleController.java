package com.hbin.mall.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.admin.domain.SysRole;
import com.hbin.mall.admin.dto.RoleQueryDTO;
import com.hbin.mall.admin.dto.SysRoleCreateDTO;
import com.hbin.mall.admin.dto.SysRoleDto;
import com.hbin.mall.admin.dto.SysRoleUpdateDTO;
import com.hbin.mall.admin.service.SysRoleService;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/role")
public class SysRoleController {

    @Autowired
    private SysRoleService roleService;

    @PostMapping("/list")
    public Result<IPage<SysRoleDto>> list(@Valid @RequestBody RoleQueryDTO query) {
        IPage<SysRoleDto> result = roleService.getRoleList(query);
        return Result.success(result);
    }

    @GetMapping("/simple-list")
    public Result<List<SysRoleDto>> getSimpleList() {
        List<SysRoleDto> result = roleService.getSimpleRoleList();
        return Result.success(result);
    }

    /**
     * 创建角色
     */
    @PostMapping
    public Result<String> create(@Valid @RequestBody SysRoleCreateDTO dto) {
        roleService.createRole(dto);
        return Result.success("角色创建成功");
    }

    /**
     * 更新角色
     */
    @PutMapping
    public Result<String> update(@Valid @RequestBody SysRoleUpdateDTO dto) {
        roleService.updateRole(dto);
        return Result.success("角色更新成功");
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@NotNull @PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success("角色删除成功");
    }

    /**
     * 获取角色权限
     */
    @GetMapping("/{roleId}/permissions")
    public Result<List<Long>> getPermissions(@NotNull @PathVariable Long roleId) {
        List<Long> permissionIds = roleService.getPermissionIdsByRoleId(roleId);
        return Result.success(permissionIds);
    }
}