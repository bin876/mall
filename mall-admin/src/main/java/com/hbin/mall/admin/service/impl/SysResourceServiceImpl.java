package com.hbin.mall.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.admin.config.ResourceLoader;
import com.hbin.mall.admin.domain.SysResource;
import com.hbin.mall.admin.dto.ResourceCreateDTO;
import com.hbin.mall.admin.dto.ResourceQueryDTO;
import com.hbin.mall.admin.dto.ResourceUpdateDTO;
import com.hbin.mall.admin.dto.SysResourceDto;
import com.hbin.mall.admin.mapper.SysResourceMapper;
import com.hbin.mall.admin.service.SysResourceService;
import com.hbin.mall.common.exception.BusinessException;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SysResourceServiceImpl extends ServiceImpl<SysResourceMapper, SysResource> implements SysResourceService {

    @Autowired
    private SysResourceMapper resourceMapper;
    
    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    public IPage<SysResourceDto> getResourceList(ResourceQueryDTO query) {
        Page<SysResource> page =
                new Page<>(query.getPageNum(), query.getPageSize());

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysResource> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getName())) {
            wrapper.like(SysResource::getName, query.getName());
        }
        if (StringUtils.hasText(query.getPath())) {
            wrapper.like(SysResource::getPath, query.getPath());
        }
        if (StringUtils.hasText(query.getPermissionCode())) {
            wrapper.like(SysResource::getPermissionCode, query.getPermissionCode());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysResource::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(SysResource::getCreateTime);

        IPage<SysResource> resourcePage = resourceMapper.selectPage(page, wrapper);

        return resourcePage.convert(this::convertToDto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createResource(ResourceCreateDTO dto) {
        boolean exists = this.lambdaQuery()
                .eq(SysResource::getPath, dto.getPath())
                .eq(SysResource::getMethod, dto.getMethod())
                .exists();

        if (exists) {
            throw new BusinessException("资源已存在");
        }

        SysResource resource = new SysResource();
        resource.setName(dto.getName());
        resource.setPath(dto.getPath());
        resource.setMethod(dto.getMethod().toUpperCase());
        resource.setPermissionCode(dto.getPermissionCode());
        resource.setStatus(1);
        resource.setCreateTime(LocalDateTime.now());
        resource.setUpdateTime(LocalDateTime.now());

        this.save(resource);

        resourceLoader.loadResourcesToRedis();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateResource(ResourceUpdateDTO dto) {
        SysResource existing = this.getById(dto.getResourceId());
        if (existing == null) {
            throw new BusinessException("资源不存在");
        }

        boolean exists = this.lambdaQuery()
                .eq(SysResource::getPath, dto.getPath())
                .eq(SysResource::getMethod, dto.getMethod())
                .ne(SysResource::getResourceId, dto.getResourceId())
                .exists();

        if (exists) {
            throw new BusinessException("资源已存在");
        }

        existing.setName(dto.getName());
        existing.setPath(dto.getPath());
        existing.setMethod(dto.getMethod().toUpperCase());
        existing.setPermissionCode(dto.getPermissionCode());
        existing.setStatus(dto.getStatus());
        existing.setUpdateTime(LocalDateTime.now());

        this.updateById(existing);

        resourceLoader.loadResourcesToRedis();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteResource(Long resourceId) {
        SysResource resource = this.getById(resourceId);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }

        this.removeById(resourceId);

        resourceLoader.loadResourcesToRedis();
    }

    private SysResourceDto convertToDto(SysResource resource) {
        SysResourceDto dto = new SysResourceDto();
        dto.setResourceId(resource.getResourceId());
        dto.setName(resource.getName());
        dto.setPath(resource.getPath());
        dto.setMethod(resource.getMethod());
        dto.setPermissionCode(resource.getPermissionCode());
        dto.setStatus(resource.getStatus());
        dto.setCreateTime(resource.getCreateTime());
        dto.setUpdateTime(resource.getUpdateTime());
        return dto;
    }
}
