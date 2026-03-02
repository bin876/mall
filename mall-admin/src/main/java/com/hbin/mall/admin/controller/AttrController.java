package com.hbin.mall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.product.dto.AttrCreateDTO;
import com.hbin.mall.api.product.dto.AttrDto;
import com.hbin.mall.api.product.dto.AttrQueryDTO;
import com.hbin.mall.api.product.dto.AttrUpdateDTO;
import com.hbin.mall.api.product.feign.AttrFeignClient;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/attr")
public class AttrController {

    @Autowired
    private AttrFeignClient attrFeignClient;

    @PostMapping("/list")
    public Result<Page<AttrDto>> getAttrList(@RequestBody AttrQueryDTO query) {
        return attrFeignClient.getAttrList(query);
    }

    @GetMapping("/{id}")
    public Result<AttrDto> getAttrById(@PathVariable Long id) {
        return attrFeignClient.getAttrById(id);
    }

    @PostMapping
    public Result<String> createAttr(@Valid @RequestBody AttrCreateDTO dto) {
        return attrFeignClient.createAttr(dto);
    }

    @PutMapping
    public Result<String> updateAttr(@Valid @RequestBody AttrUpdateDTO dto) {
        return attrFeignClient.updateAttr(dto);
    }

    @DeleteMapping
    public Result<String> deleteAttrs(@RequestParam List<Long> ids) {
        return attrFeignClient.deleteAttrs(ids);
    }
}