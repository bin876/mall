package com.hbin.mall.admin.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hbin.mall.admin.dto.PermissionQueryDTO;
import com.hbin.mall.admin.dto.SysPermissionCreateDTO;
import com.hbin.mall.admin.dto.SysPermissionDto;
import com.hbin.mall.admin.dto.SysPermissionUpdateDTO;
import com.hbin.mall.admin.service.SysPermissionService;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/permission")
public class SysPermissionController {

    @Autowired
    private SysPermissionService permissionService;

    @PostMapping("/list")
    public Result<IPage<SysPermissionDto>> list(@Valid @RequestBody PermissionQueryDTO query) {
        IPage<SysPermissionDto> result = permissionService.getPermissionList(query);
        return Result.success(result);
    }

    @GetMapping("/simple-list")
    public Result<List<SysPermissionDto>> getSimpleList() {
        List<SysPermissionDto> result = permissionService.getSimplePermissionList();
        return Result.success(result);
    }

    /**
     * 创建权限
     */
    @PostMapping
    public Result<String> create(@Valid @RequestBody SysPermissionCreateDTO dto) {
        permissionService.createPermission(dto);
        return Result.success("权限创建成功");
    }

    /**
     * 更新权限
     */
    @PutMapping
    public Result<String> update(@Valid @RequestBody SysPermissionUpdateDTO dto) {
        permissionService.updatePermission(dto);
        return Result.success("权限更新成功");
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@NotNull @PathVariable Long id) {
        permissionService.deletePermission(id);
        return Result.success("权限删除成功");
    }
}