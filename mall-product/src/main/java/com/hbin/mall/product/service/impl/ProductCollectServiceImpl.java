package com.hbin.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.common.exception.BusinessException;
import com.hbin.mall.product.domain.ProductCollect;
import com.hbin.mall.product.domain.Spu;
import com.hbin.mall.product.dto.CollectCreateDTO;
import com.hbin.mall.product.dto.CollectDTO;
import com.hbin.mall.product.mapper.ProductCollectMapper;
import com.hbin.mall.product.mapper.SpuMapper;
import com.hbin.mall.product.service.ProductCollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductCollectServiceImpl extends ServiceImpl<ProductCollectMapper, ProductCollect> implements ProductCollectService {

    @Autowired
    private ProductCollectMapper collectMapper;

    @Autowired
    private SpuMapper spuMapper;

    @Override
    public Page<CollectDTO> getCollectList(Long memberId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ProductCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCollect::getMemberId, memberId)
                .orderByDesc(ProductCollect::getCreateTime);

        Page<ProductCollect> page = new Page<>(pageNum, pageSize);
        IPage<ProductCollect> collectPage = collectMapper.selectPage(page, wrapper);

        List<CollectDTO> dtos = collectPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new Page<CollectDTO>()
                .setCurrent(pageNum)
                .setSize(pageSize)
                .setTotal(collectPage.getTotal())
                .setRecords(dtos);
    }

    @Override
    @Transactional
    public void collectProduct(Long memberId, CollectCreateDTO dto) {
        Spu spu = spuMapper.selectById(dto.getSpuId());
        if (spu == null) {
            throw new BusinessException("商品不存在");
        }

        if (checkCollected(memberId, dto.getSpuId())) {
            throw new BusinessException("商品已收藏");
        }

        ProductCollect collect = new ProductCollect();
        collect.setMemberId(memberId);
        collect.setSpuId(spu.getSpuId());
        collect.setSpuName(spu.getName());
        collect.setSpuPic(spu.getDefaultImg());
        collect.setSpuPrice(spu.getMinPrice());
        collect.setCreateTime(LocalDateTime.now());

        collectMapper.insert(collect);
    }

    @Override
    @Transactional
    public void cancelCollect(Long memberId, Long collectId) {
        ProductCollect collect = collectMapper.selectById(collectId);
        if (collect == null || !collect.getMemberId().equals(memberId)) {
            throw new BusinessException("收藏记录不存在或无权删除");
        }

        collectMapper.deleteById(collectId);
    }

    @Override
    @Transactional
    public void cancelCollectBySpuId(Long memberId, Long spuId) {
        LambdaQueryWrapper<ProductCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCollect::getMemberId, memberId)
                .eq(ProductCollect::getSpuId, spuId);

        collectMapper.delete(wrapper);
    }

    @Override
    @Transactional
    public void batchCancelCollect(Long memberId, List<Long> collectIds) {
        if (collectIds == null || collectIds.isEmpty()) {
            return;
        }

        List<ProductCollect> collects = collectMapper.selectBatchIds(collectIds);
        for (ProductCollect collect : collects) {
            if (!collect.getMemberId().equals(memberId)) {
                throw new BusinessException("收藏失败");
            }
        }

        collectMapper.deleteBatchIds(collectIds);
    }

    @Override
    public Boolean checkCollected(Long memberId, Long spuId) {
        if (memberId == null || spuId == null) {
            return false;
        }

        return collectMapper.exists(
                new LambdaQueryWrapper<ProductCollect>()
                        .eq(ProductCollect::getMemberId, memberId)
                        .eq(ProductCollect::getSpuId, spuId)
        );
    }

    @Override
    public Long getCollectCount(Long memberId) {
        return collectMapper.selectCount(
                new LambdaQueryWrapper<ProductCollect>().eq(ProductCollect::getMemberId, memberId)
        );
    }

    private CollectDTO convertToDTO(ProductCollect collect) {
        CollectDTO dto = new CollectDTO();
        dto.setCollectId(collect.getCollectId());
        dto.setSpuId(collect.getSpuId());
        dto.setSpuName(collect.getSpuName());
        dto.setSpuPic(collect.getSpuPic());
        dto.setSpuPrice(collect.getSpuPrice());
        dto.setCreateTime(collect.getCreateTime());
        return dto;
    }
}
