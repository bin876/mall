package com.hbin.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.api.product.dto.BatchUpdateSkuDTO;
import com.hbin.mall.api.product.dto.SkuDTO;
import com.hbin.mall.api.product.dto.SkuQueryDTO;
import com.hbin.mall.api.product.dto.UpdateSkuDTO;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.product.domain.Sku;
import com.hbin.mall.product.domain.Attr;
import com.hbin.mall.product.domain.SkuAttrValue;
import com.hbin.mall.product.domain.Spu;
import com.hbin.mall.product.mapper.AttrMapper;
import com.hbin.mall.product.mapper.SkuAttrValueMapper;
import com.hbin.mall.product.mapper.SkuMapper;
import com.hbin.mall.product.mapper.SpuMapper;
import com.hbin.mall.product.service.SkuService;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SkuServiceImpl extends ServiceImpl<SkuMapper, Sku> implements SkuService {

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private AttrMapper attrMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PRODUCT_DETAIL_CACHE_KEY_PREFIX = "product:detail:";

    @Override
    public Result<SkuDTO> getSkuById(Long skuId) {
        Sku sku = skuMapper.selectOne(
                new LambdaQueryWrapper<Sku>()
                        .eq(Sku::getSkuId, skuId)
        );

        if (sku == null) {
            return Result.error("商品不存在");
        }

        SkuDTO dto = new SkuDTO();
        dto.setSkuId(sku.getSkuId());
        dto.setName(sku.getName());
        dto.setPrice(sku.getPrice());
        dto.setDefaultImg(sku.getDefaultImg());

        return Result.success(dto);
    }

    @Override
    @Transactional
    public boolean lockStock(Long skuId, Integer quantity) {
        int updated = skuMapper.update(null,
                new UpdateWrapper<Sku>()
                        .setSql("stock_locked = stock_locked + " + quantity)
                        .eq("sku_id", skuId)
                        .ge("stock", quantity)
        );
        return updated > 0;
    }

    @Override
    @Transactional
    public boolean deductStock(Long skuId, Integer quantity) {
        int updated = skuMapper.update(null,
                new UpdateWrapper<Sku>()
                        .setSql("stock = stock - " + quantity + ", stock_locked = stock_locked - " + quantity)
                        .eq("sku_id", skuId)
                        .ge("stock_locked", quantity)
        );

        return updated > 0;
    }

    @Override
    public Page<SkuDTO> getSkuList(SkuQueryDTO query) {
        Page<Sku> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Sku> wrapper = buildQueryWrapper(query);
        IPage<Sku> skuPage = skuMapper.selectPage(page, wrapper);

        List<Long> spuIds = skuPage.getRecords().stream()
                .map(Sku::getSpuId)
                .collect(Collectors.toList());
        Map<Long, String> spuNameMap = getSpuNameMap(spuIds);

        List<Long> skuIds = skuPage.getRecords().stream()
                .map(Sku::getSkuId)
                .collect(Collectors.toList());
        Map<Long, List<SkuDTO.SaleAttrDTO>> attrMap = getSaleAttrsMap(skuIds);

        return (Page<SkuDTO>) skuPage.convert(sku -> {
            SkuDTO dto = new SkuDTO();
            dto.setSkuId(sku.getSkuId());
            dto.setSpuId(sku.getSpuId());
            dto.setName(sku.getName());
            dto.setTitle(sku.getTitle());
            dto.setSubtitle(sku.getSubtitle());
            dto.setPrice(sku.getPrice());
            dto.setStock(sku.getStock());
            dto.setStatus(sku.getStatus());
            dto.setDefaultImg(sku.getDefaultImg());
            dto.setCreateTime(sku.getCreateTime());
            dto.setUpdateTime(sku.getUpdateTime());
            dto.setSpuName(spuNameMap.get(sku.getSpuId()));
            dto.setSaleAttrs(attrMap.get(sku.getSkuId()));
            return dto;
        });
    }
    @Override
    public void updateSku(Long id, UpdateSkuDTO dto) {
        Sku sku = skuMapper.selectById(id);
        if (sku == null) {
            throw new IllegalArgumentException("SKU不存在");
        }

        sku.setName(dto.getName());
        sku.setTitle(dto.getTitle());
        sku.setSubtitle(dto.getSubtitle());
        sku.setPrice(dto.getPrice());
        sku.setStock(dto.getStock());
        sku.setStatus(dto.getStatus());
        skuMapper.updateById(sku);

        clearSpuCache(sku.getSpuId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateSku(BatchUpdateSkuDTO dto) {
        if (dto.getSkuIds() == null || dto.getSkuIds().isEmpty()) {
            return;
        }

        LambdaUpdateWrapper<Sku> wrapper = new LambdaUpdateWrapper<>();

        if (dto.getPrice() != null) {
            if ("set".equals(dto.getPriceOperation())) {
                wrapper.set(Sku::getPrice, dto.getPrice());
            } else if ("add".equals(dto.getPriceOperation())) {
                wrapper.setSql("price = price + " + dto.getPrice());
            } else if ("reduce".equals(dto.getPriceOperation())) {
                wrapper.setSql("price = price - " + dto.getPrice());
            }
        }

        if (dto.getStock() != null) {
            if ("set".equals(dto.getStockOperation())) {
                wrapper.set(Sku::getStock, dto.getStock());
            } else if ("add".equals(dto.getStockOperation())) {
                wrapper.setSql("stock = stock + " + dto.getStock());
            } else if ("reduce".equals(dto.getStockOperation())) {
                wrapper.setSql("stock = stock - " + dto.getStock());
            }
        }

        if (dto.getStatus() != null) {
            wrapper.set(Sku::getStatus, dto.getStatus());
        }

        wrapper.in(Sku::getSkuId, dto.getSkuIds());
        skuMapper.update(null, wrapper);
    }

    @Override
    public void deleteSku(Long id) {
        skuMapper.deleteById(id);
        skuAttrValueMapper.delete(
                new LambdaQueryWrapper<SkuAttrValue>().eq(SkuAttrValue::getSkuId, id)
        );
        clearSpuCache(id);
    }

    @Override
    public List<SkuDTO> getSkusBySpuId(Long spuId) {
        if (spuId == null) {
            return Collections.emptyList();
        }

        List<Sku> skus = skuMapper.selectList(
                new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, spuId)
        );

        if (skus.isEmpty()) {
            return Collections.emptyList();
        }

        String spuName = getSPUNameById(spuId);

        List<SkuDTO> dtoList = skus.stream()
                .map(sku -> convertToDTO(sku, spuName))
                .collect(Collectors.toList());

        List<Long> skuIds = dtoList.stream()
                .map(SkuDTO::getSkuId)
                .collect(Collectors.toList());

        if (!skuIds.isEmpty()) {
            List<SkuAttrValue> allAttrValues = skuAttrValueMapper.selectBatchIds(skuIds);

            Map<Long, List<SkuAttrValue>> attrValuesMap = allAttrValues.stream()
                    .collect(Collectors.groupingBy(SkuAttrValue::getSkuId));

            for (SkuDTO dto : dtoList) {
                List<SkuAttrValue> attrValues = attrValuesMap.get(dto.getSkuId());
                if (attrValues != null) {
                    List<SkuDTO.SaleAttrDTO> saleAttrs = attrValues.stream()
                            .map(attrValue -> {
                                SkuDTO.SaleAttrDTO saleAttr = new SkuDTO.SaleAttrDTO();
                                saleAttr.setAttrId(attrValue.getAttrId());
                                saleAttr.setAttrName(attrValue.getAttrName());
                                saleAttr.setAttrValue(attrValue.getAttrValue());
                                return saleAttr;
                            })
                            .collect(Collectors.toList());
                    dto.setSaleAttrs(saleAttrs);
                }
            }
        }

        return dtoList;
    }

    private SkuDTO convertToDTO(Sku sku, String spuName) {
        SkuDTO dto = new SkuDTO();
        dto.setSkuId(sku.getSkuId());
        dto.setSpuId(sku.getSpuId());
        dto.setSpuName(spuName);
        dto.setName(sku.getName());
        dto.setTitle(sku.getTitle());
        dto.setSubtitle(sku.getSubtitle());
        dto.setPrice(sku.getPrice());
        dto.setStock(sku.getStock());
        dto.setDefaultImg(sku.getDefaultImg());
        dto.setStatus(sku.getStatus());
        dto.setCreateTime(sku.getCreateTime());
        dto.setUpdateTime(sku.getUpdateTime());
        return dto;
    }

    public String getSPUNameById(Long spuId) {
        if (spuId == null) {
            return null;
        }
        Spu spu = spuMapper.selectById(spuId);
        return spu != null ? spu.getName() : null;
    }

    private SkuDTO convertToDTO(Sku sku) {
        SkuDTO dto = new SkuDTO();
        dto.setSkuId(sku.getSkuId());
        dto.setSpuId(sku.getSpuId());
        dto.setName(sku.getName());
        dto.setTitle(sku.getTitle());
        dto.setSubtitle(sku.getSubtitle());
        dto.setPrice(sku.getPrice());
        dto.setStock(sku.getStock());
        dto.setStatus(sku.getStatus());
        dto.setDefaultImg(sku.getDefaultImg());
        dto.setCreateTime(sku.getCreateTime());
        dto.setUpdateTime(sku.getUpdateTime());
        return dto;
    }

    private LambdaQueryWrapper<Sku> buildQueryWrapper(SkuQueryDTO query) {
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();

        if (query.getSpuId() != null) {
            wrapper.eq(Sku::getSpuId, query.getSpuId());
        }

        if (query.getSpuName() != null) {
            wrapper.exists(
                    "SELECT 1 FROM spu s WHERE s.spu_id = sku.spu_id AND s.name LIKE {0}",
                    "%" + query.getSpuName() + "%"
            );
        }

        if (query.getCategoryId() != null) {
            wrapper.exists(
                    "SELECT 1 FROM spu s WHERE s.spu_id = sku.spu_id AND s.category_id = {0}",
                    query.getCategoryId()
            );
        }

        if (query.getBrandId() != null) {
            wrapper.exists(
                    "SELECT 1 FROM spu s WHERE s.spu_id = sku.spu_id AND s.brand_id = {0}",
                    query.getBrandId()
            );
        }

        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.like(Sku::getName, query.getKeyword());
        }

        if (query.getStatus() != null) {
            wrapper.eq(Sku::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(Sku::getSkuId);
        return wrapper;
    }

    private Map<Long, String> getSpuNameMap(List<Long> spuIds) {
        if (spuIds.isEmpty()) return Collections.emptyMap();
        List<Spu> spus = spuMapper.selectBatchIds(spuIds);
        return spus.stream()
                .collect(Collectors.toMap(Spu::getSpuId, Spu::getName));
    }

    private Map<Long, List<SkuDTO.SaleAttrDTO>> getSaleAttrsMap(List<Long> skuIds) {
        if (skuIds.isEmpty()) return Collections.emptyMap();

        List<SkuAttrValue> attrValues = skuAttrValueMapper.selectList(
                new LambdaQueryWrapper<SkuAttrValue>().in(SkuAttrValue::getSkuId, skuIds)
        );

        List<Long> attrIds = attrValues.stream()
                .map(SkuAttrValue::getAttrId)
                .collect(Collectors.toList());
        Map<Long, String> attrNameMap = attrMapper.selectBatchIds(attrIds).stream()
                .collect(Collectors.toMap(Attr::getAttrId, Attr::getAttrName));

        return attrValues.stream()
                .collect(Collectors.groupingBy(
                        SkuAttrValue::getSkuId,
                        Collectors.mapping(v -> {
                            SkuDTO.SaleAttrDTO attr = new SkuDTO.SaleAttrDTO();
                            attr.setAttrId(v.getAttrId());
                            attr.setAttrName(attrNameMap.get(v.getAttrId()));
                            attr.setAttrValue(v.getAttrValue());
                            return attr;
                        }, Collectors.toList())
                ));
    }

    private void clearSpuCache(Long spuId) {
        if (spuId != null && spuId > 0) {
            String cacheKey = PRODUCT_DETAIL_CACHE_KEY_PREFIX + spuId;
            redisTemplate.delete(cacheKey);
        }
    }
}
