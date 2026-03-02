package com.hbin.mall.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.admin.domain.SysResource;
import com.hbin.mall.admin.dto.ResourceCreateDTO;
import com.hbin.mall.admin.dto.ResourceQueryDTO;
import com.hbin.mall.admin.dto.ResourceUpdateDTO;
import com.hbin.mall.admin.dto.SysResourceDto;
import org.springframework.transaction.annotation.Transactional;

public interface SysResourceService extends IService<SysResource> {

    IPage<SysResourceDto> getResourceList(ResourceQueryDTO query);

    @Transactional(rollbackFor = Exception.class)
    void createResource(ResourceCreateDTO dto);

    @Transactional(rollbackFor = Exception.class)
    void updateResource(ResourceUpdateDTO dto);

    @Transactional(rollbackFor = Exception.class)
    void deleteResource(Long resourceId);
}
