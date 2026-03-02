package com.hbin.mall.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.product.dto.AttrDto;
import com.hbin.mall.api.product.dto.AttrQueryDTO;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.product.service.AttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product/attr")
public class AttrController {

    @Autowired
    private AttrService attrService;

    @PostMapping("/list")
    public Result<Page<AttrDto>> getAttrList(@RequestBody AttrQueryDTO query) {
        return Result.success(attrService.getAttrList(query));
    }
}
