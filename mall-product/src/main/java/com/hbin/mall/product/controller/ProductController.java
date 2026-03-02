package com.hbin.mall.product.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hbin.mall.api.product.dto.SpuDTO;
import com.hbin.mall.api.product.dto.SpuDetailDTO;
import com.hbin.mall.api.product.dto.SpuQueryDTO;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.product.domain.SpuDocument;
import com.hbin.mall.product.dto.ProductListQuery;
import com.hbin.mall.product.dto.SpuSimpleDTO;
import com.hbin.mall.product.service.SearchService;
import com.hbin.mall.product.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import static cn.dev33.satoken.SaManager.log;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private SpuService spuService;

    @Autowired
    private SearchService searchService;

    @GetMapping("/search")
    public Result<IPage<SpuSimpleDTO>> searchProductList(ProductListQuery query) {
        try {
            IPage<SpuSimpleDTO> page = searchService.searchProducts(query);
            return Result.success(page);
        } catch (Exception e) {
            log.error("ES搜索失败，降级到MySQL", e);
            return getProductList(query);
        }
    }

    @GetMapping("/recommend")
    public Result<IPage<SpuSimpleDTO>> getRecommendSpu(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        IPage<SpuSimpleDTO> page = spuService.getRecommendSpu(pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/{spuId}")
    public Result<SpuDetailDTO> getSpuDetail(@PathVariable Long spuId) {
        SpuDetailDTO detail = spuService.getSpuDetail(spuId);
        return Result.success(detail);
    }

    @GetMapping("/list")
    public Result<IPage<SpuSimpleDTO>> getProductList(ProductListQuery query) {
        IPage<SpuSimpleDTO> page = spuService.getProductList(query);
        return Result.success(page);
    }
}