package com.hbin.mall.admin.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.admin.domain.SysUser;
import com.hbin.mall.admin.dto.*;
import com.hbin.mall.admin.service.SysUserService;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.common.satoken.StpAdminUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/user")
public class SysUserController {

    @Autowired
    private SysUserService userService;

    @GetMapping("/info")
    public Result<SysUserInfoDTO> getAdminInfo() {
        Long userId = StpAdminUtil.getLoginIdAsLong();
        SysUserInfoDTO info = userService.getSysUserInfo(userId);
        return Result.success(info);
    }

    @PutMapping("/info")
    public Result<String> updateAdminInfo(@RequestBody SysUserInfoDTO dto) {
        Long userId = StpAdminUtil.getLoginIdAsLong();
        userService.updateSysUserInfo(userId, dto);
        return Result.success("管理员信息更新成功");
    }

    @GetMapping("/list")
    public Result<Page<SysUser>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        Page<SysUser> result = userService.page(page);
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }

    @PostMapping("/list")
    public Result<IPage<SysUserDto>> list(@Valid @RequestBody UserQueryDTO query) {
        IPage<SysUserDto> result = userService.getUserList(query);
        return Result.success(result);
    }

    @GetMapping("/simple-list")
    public Result<List<SysUserDto>> getSimpleList() {
        List<SysUserDto> result = userService.getSimpleUserList();
        return Result.success(result);
    }

    @PostMapping
    public Result<String> create(@Valid @RequestBody SysUserCreateDTO dto) {
        userService.createUser(dto);
        return Result.success("用户创建成功");
    }

    @PutMapping
    public Result<String> update(@Valid @RequestBody SysUserUpdateDTO dto) {
        userService.updateUser(dto);
        return Result.success("用户更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@NotNull @PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("用户删除成功");
    }

    @GetMapping("/{userId}/roles")
    public Result<List<Long>> getRoles(@NotNull @PathVariable Long userId) {
        List<Long> roleIds = userService.getRoleIdsByUserId(userId);
        return Result.success(roleIds);
    }

    @GetMapping("/permissions")
    public Result<List<String>> getPermissions() {
        Long userId = StpAdminUtil.getLoginIdAsLong();
        List<String> permissionCodes = userService.getUserPermissionCodes(userId);
        return Result.success(permissionCodes);
    }
}
