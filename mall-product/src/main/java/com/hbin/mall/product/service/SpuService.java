package com.hbin.mall.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.api.product.dto.*;
import com.hbin.mall.product.domain.Spu;
import com.hbin.mall.product.dto.ProductListQuery;
import com.hbin.mall.product.dto.SpuSimpleDTO;
import org.springframework.transaction.annotation.Transactional;

public interface SpuService extends IService<Spu> {

    IPage<SpuSimpleDTO> getRecommendSpu(int pageNum, int pageSize);

    // TODO
    IPage<SpuSimpleDTO> getProductList(ProductListQuery query);

    SpuDetailDTO getSpuDetail(Long spuId);

    void updateSaleCount(Long spuId, Integer quantity);

    @Transactional
    void publishSpu(PublishSpuRequest request);

    Page<SpuDTO> getSpuList(SpuQueryDTO query);

    @Transactional(rollbackFor = Exception.class)
    void updateSpu(UpdateSpuDTO dto);

    void updateSpuStatus(Long id, Integer publishStatus);

    void deleteSpu(Long id);

    String getSPUName(Long spuId);

    SpuDTO getSPUById(Long spuId);
}
