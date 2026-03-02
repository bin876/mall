package com.hbin.mall.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hbin.mall.admin.domain.SysResource;
import com.hbin.mall.admin.dto.ResourceCreateDTO;
import com.hbin.mall.admin.dto.ResourceQueryDTO;
import com.hbin.mall.admin.dto.ResourceUpdateDTO;
import com.hbin.mall.admin.dto.SysResourceDto;
import com.hbin.mall.admin.service.SysResourceService;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/resource")
public class ResourceController {

    @Autowired
    private SysResourceService resourceService;

    @PostMapping("/list")
    public Result<IPage<SysResourceDto>> getResourceList(@Valid @RequestBody ResourceQueryDTO query) {
        IPage<SysResourceDto> result = resourceService.getResourceList(query);
        return Result.success(result);
    }

    @PostMapping
    public Result<String> createResource(@Valid @RequestBody ResourceCreateDTO dto) {
        resourceService.createResource(dto);
        return Result.success("资源创建成功");
    }

    @PutMapping
    public Result<String> updateResource(@Valid @RequestBody ResourceUpdateDTO dto) {
        resourceService.updateResource(dto);
        return Result.success("资源更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteResource(@NotNull @PathVariable Long id) {
        resourceService.deleteResource(id);
        return Result.success("资源删除成功");
    }
}