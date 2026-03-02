package com.hbin.mall.api.product.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.product.dto.*;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "mall-product", contextId = "attr")
public interface AttrFeignClient {

    @PostMapping("/inner/attr/list")
    Result<Page<AttrDto>> getAttrList(@RequestBody AttrQueryDTO query);

    @GetMapping("/inner/attr/{id}")
    Result<AttrDto> getAttrById(@PathVariable Long id);

    @PostMapping("/inner/attr")
    Result<String> createAttr(@Valid @RequestBody AttrCreateDTO dto);

    @PutMapping("/inner/attr")
    Result<String> updateAttr(@Valid @RequestBody AttrUpdateDTO dto);

    @DeleteMapping("/inner/attr")
    Result<String> deleteAttrs(@RequestParam List<Long> ids);
}
