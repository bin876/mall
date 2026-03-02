package com.hbin.mall.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.api.product.dto.BatchUpdateSkuDTO;
import com.hbin.mall.api.product.dto.SkuDTO;
import com.hbin.mall.api.product.dto.SkuQueryDTO;
import com.hbin.mall.api.product.dto.UpdateSkuDTO;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.product.domain.Sku;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SkuService extends IService<Sku> {

    Result<SkuDTO> getSkuById(Long skuId);

    @Transactional
    boolean lockStock(Long skuId, Integer quantity);

    @Transactional
    boolean deductStock(Long skuId, Integer quantity);

    Page<SkuDTO> getSkuList(SkuQueryDTO query);

    void updateSku(Long id, UpdateSkuDTO dto);

    @Transactional(rollbackFor = Exception.class)
    void batchUpdateSku(BatchUpdateSkuDTO dto);

    void deleteSku(Long id);

    List<SkuDTO> getSkusBySpuId(Long spuId);
}
