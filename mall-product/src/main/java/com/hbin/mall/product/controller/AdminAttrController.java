package com.hbin.mall.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.product.dto.AttrCreateDTO;
import com.hbin.mall.api.product.dto.AttrDto;
import com.hbin.mall.api.product.dto.AttrQueryDTO;
import com.hbin.mall.api.product.dto.AttrUpdateDTO;
import com.hbin.mall.api.product.feign.AttrFeignClient;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.product.service.AttrService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AdminAttrController implements AttrFeignClient {

    @Autowired
    private AttrService attrService;

    @Override
    public Result<Page<AttrDto>> getAttrList(@RequestBody AttrQueryDTO query) {
        return Result.success(attrService.getAttrList(query));
    }

    @Override
    public Result<AttrDto> getAttrById(@PathVariable Long id) {
        return Result.success(attrService.getAttrById(id));
    }

    @Override
    public Result<String> createAttr(@Valid @RequestBody AttrCreateDTO dto) {
        attrService.createAttr(dto);
        return Result.success("属性创建成功");
    }

    @Override
    public Result<String> updateAttr(@Valid @RequestBody AttrUpdateDTO dto) {
        attrService.updateAttr(dto);
        return Result.success("属性更新成功");
    }

    @Override
    public Result<String> deleteAttrs(@RequestParam List<Long> ids) {
        attrService.deleteAttrs(ids);
        return Result.success("属性删除成功");
    }
}