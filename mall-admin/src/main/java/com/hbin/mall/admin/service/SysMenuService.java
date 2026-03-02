package com.hbin.mall.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.admin.domain.SysMenu;
import com.hbin.mall.admin.dto.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface SysMenuService extends IService<SysMenu> {

    IPage<SysMenuDto> getMenuList(MenuQueryDTO query);

    List<SysMenuDto> getMenuTreeByUserId(Long userId);

    Set<String> getUserPermissionCodes(Long userId);

    @Transactional(rollbackFor = Exception.class)
    void createMenu(SysMenuCreateDTO dto);

    @Transactional(rollbackFor = Exception.class)
    void updateMenu(SysMenuUpdateDTO dto);

    @Transactional(rollbackFor = Exception.class)
    void deleteMenu(Long menuId);
}
