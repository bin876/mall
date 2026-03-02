package com.hbin.mall.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.product.domain.ProductCollect;
import com.hbin.mall.product.dto.CollectCreateDTO;
import com.hbin.mall.product.dto.CollectDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductCollectService extends IService<ProductCollect> {

    Page<CollectDTO> getCollectList(Long memberId, Integer pageNum, Integer pageSize);

    @Transactional
    void collectProduct(Long memberId, CollectCreateDTO dto);

    @Transactional
    void cancelCollect(Long memberId, Long collectId);

    @Transactional
    void cancelCollectBySpuId(Long memberId, Long spuId);

    @Transactional
    void batchCancelCollect(Long memberId, List<Long> collectIds);

    Boolean checkCollected(Long memberId, Long spuId);

    Long getCollectCount(Long memberId);
}
