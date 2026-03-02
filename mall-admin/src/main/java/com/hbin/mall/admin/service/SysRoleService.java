package com.hbin.mall.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.admin.domain.SysRole;
import com.hbin.mall.admin.dto.RoleQueryDTO;
import com.hbin.mall.admin.dto.SysRoleCreateDTO;
import com.hbin.mall.admin.dto.SysRoleDto;
import com.hbin.mall.admin.dto.SysRoleUpdateDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    IPage<SysRoleDto> getRoleList(RoleQueryDTO query);

    List<SysRoleDto> getSimpleRoleList();

    @Transactional
    void createRole(SysRoleCreateDTO dto);

    @Transactional
    void updateRole(SysRoleUpdateDTO dto);

    @Transactional
    void deleteRole(Long roleId);

    List<Long> getPermissionIdsByRoleId(Long roleId);
}
