package com.hbin.mall.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.admin.domain.SysPermission;
import com.hbin.mall.admin.dto.PermissionQueryDTO;
import com.hbin.mall.admin.dto.SysPermissionCreateDTO;
import com.hbin.mall.admin.dto.SysPermissionDto;
import com.hbin.mall.admin.dto.SysPermissionUpdateDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SysPermissionService extends IService<SysPermission> {

    IPage<SysPermissionDto> getPermissionList(PermissionQueryDTO query);

    List<SysPermissionDto> getSimplePermissionList();

    @Transactional(rollbackFor = Exception.class)
    void createPermission(SysPermissionCreateDTO dto);

    @Transactional(rollbackFor = Exception.class)
    void updatePermission(SysPermissionUpdateDTO dto);

    @Transactional(rollbackFor = Exception.class)
    void deletePermission(Long permissionId);
}
