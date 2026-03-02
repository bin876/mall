package com.hbin.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.api.product.dto.*;
import com.hbin.mall.common.exception.BusinessException;
import com.hbin.mall.product.domain.*;
import com.hbin.mall.product.dto.*;
import com.hbin.mall.product.mapper.*;
import com.hbin.mall.product.service.SpuService;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SpuServiceImpl extends ServiceImpl<SpuMapper, Spu> implements SpuService {

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuAttrValueMapper spuAttrValueMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private AttrMapper attrMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PRODUCT_DETAIL_CACHE_KEY_PREFIX = "product:detail:";
    private static final long CACHE_EXPIRE_MINUTES = 5;

    @Override
    @DS("slave")
    public IPage<SpuSimpleDTO> getRecommendSpu(int pageNum, int pageSize) {
        Page<Spu> page = new Page<>(pageNum, pageSize);

        IPage<Spu> spuPage = this.page(page,
                new LambdaQueryWrapper<Spu>()
                        .orderByDesc(Spu::getCreateTime)
        );

        return spuPage.convert(spu -> {
            SpuSimpleDTO dto = new SpuSimpleDTO();
            dto.setSpuId(spu.getSpuId());
            dto.setName(spu.getName());
            dto.setDefaultImg(spu.getDefaultImg());

            BigDecimal minPrice = skuMapper.selectList(
                            new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, spu.getSpuId())
                    ).stream()
                    .map(Sku::getPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            dto.setMinPrice(minPrice);
            dto.setSaleCount(spu.getSaleCount());

            return dto;
        });
    }

    @Override
    public SpuDetailDTO getSpuDetail(Long spuId) {
        if (spuId == null || spuId <= 0) {
            throw new IllegalArgumentException("SPU ID 不能为空");
        }

        String cacheKey = PRODUCT_DETAIL_CACHE_KEY_PREFIX + spuId;
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);

        if (cacheValue != null) {
            if ("null".equals(cacheValue)) {
                throw new BusinessException("商品不存在");
            }
            return JSON.parseObject(cacheValue, SpuDetailDTO.class);
        }

        SpuDetailDTO dto = getSpuDetailFromDB(spuId);

        if (dto == null) {
            redisTemplate.opsForValue().set(
                    cacheKey,
                    "null",
                    Duration.ofMinutes(5)
            );
            throw new BusinessException("商品不存在");
        }

        String json = JSON.toJSONString(dto);
        redisTemplate.opsForValue().set(
                cacheKey,
                json,
                Duration.ofMinutes(CACHE_EXPIRE_MINUTES)
        );

        return dto;
    }

    private SpuDetailDTO getSpuDetailFromDB(Long spuId) {
        Spu spu = this.getById(spuId);
        if (spu == null) {
            return null;
        }

        SpuDetailDTO dto = new SpuDetailDTO();

        SpuDetailDTO.Spu spuDto = new SpuDetailDTO.Spu();
        spuDto.setSpuId(spu.getSpuId());
        spuDto.setName(spu.getName());
        spuDto.setDescription(spu.getDescription());
        spuDto.setCategoryId(spu.getCategoryId());
        spuDto.setBrandId(spu.getBrandId());
        spuDto.setWeight(spu.getWeight());
        spuDto.setPublishStatus(spu.getPublishStatus());
        spuDto.setDetail(spu.getDetail());
        spuDto.setDefaultImg(spu.getDefaultImg());
        dto.setSpu(spuDto);

        List<SpuImage> spuImageList = spuImageMapper.selectList(
                new LambdaQueryWrapper<SpuImage>().eq(SpuImage::getSpuId, spuId)
        );
        dto.setSpuImages(spuImageList.stream()
                .map(img -> {
                    SpuDetailDTO.ImageDTO imgDto = new SpuDetailDTO.ImageDTO();
                    imgDto.setUrl(img.getImgUrl());
                    imgDto.setSort(img.getSort());
                    return imgDto;
                })
                .sorted(Comparator.comparing(SpuDetailDTO.ImageDTO::getSort))
                .collect(Collectors.toList()));

        List<SpuAttrValue> spuAttrList = spuAttrValueMapper.selectList(
                new LambdaQueryWrapper<SpuAttrValue>().eq(SpuAttrValue::getSpuId, spuId)
        );
        dto.setSpuAttrs(spuAttrList.stream()
                .map(attr -> {
                    SpuDetailDTO.AttrValueDTO attrDto = new SpuDetailDTO.AttrValueDTO();
                    attrDto.setAttrId(attr.getAttrId());
                    attrDto.setAttrName(attr.getAttrName());
                    attrDto.setAttrValue(attr.getAttrValue());
                    return attrDto;
                })
                .collect(Collectors.toList()));

        List<Sku> skuList = skuMapper.selectList(
                new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, spuId)
        );
        dto.setSkus(skuList.stream()
                .map(sku -> {
                    SpuDetailDTO.SkuDTO skuDto = new SpuDetailDTO.SkuDTO();
                    skuDto.setSkuId(sku.getSkuId());
                    skuDto.setName(sku.getName());
                    skuDto.setTitle(sku.getTitle());
                    skuDto.setSubtitle(sku.getSubtitle());
                    skuDto.setPrice(sku.getPrice());
                    skuDto.setStock(sku.getStock());
                    skuDto.setDefaultImg(sku.getDefaultImg());

                    List<SkuAttrValue> skuAttrList = skuAttrValueMapper.selectList(
                            new LambdaQueryWrapper<SkuAttrValue>().eq(SkuAttrValue::getSkuId, sku.getSkuId())
                    );
                    skuDto.setAttrs(skuAttrList.stream()
                            .map(attr -> {
                                SpuDetailDTO.AttrValueDTO attrDto = new SpuDetailDTO.AttrValueDTO();
                                attrDto.setAttrId(attr.getAttrId());
                                attrDto.setAttrName(attr.getAttrName());
                                attrDto.setAttrValue(attr.getAttrValue());
                                return attrDto;
                            })
                            .collect(Collectors.toList()));

                    List<SkuImage> skuImageList = skuImageMapper.selectList(
                            new LambdaQueryWrapper<SkuImage>().eq(SkuImage::getSkuId, sku.getSkuId())
                    );
                    skuDto.setImages(skuImageList.stream()
                            .map(img -> {
                                SpuDetailDTO.ImageDTO imgDto = new SpuDetailDTO.ImageDTO();
                                imgDto.setUrl(img.getImgUrl());
                                imgDto.setSort(img.getSort());
                                return imgDto;
                            })
                            .sorted(Comparator.comparing(SpuDetailDTO.ImageDTO::getSort))
                            .collect(Collectors.toList()));

                    return skuDto;
                })
                .collect(Collectors.toList()));

        return dto;
    }

    @Override
    @Transactional
    public void publishSpu(PublishSpuRequest request) {
        Spu spu = new Spu();
        spu.setName(request.getName());
        spu.setDescription(request.getDescription());
        spu.setCategoryId(request.getCategoryId());
        spu.setBrandId(request.getBrandId());
        spu.setWeight(request.getWeight());
        spu.setPublishStatus(0);
        spu.setDetail(request.getDetail());
        spu.setCreateTime(LocalDateTime.now());
        spu.setUpdateTime(LocalDateTime.now());

        if (request.getSpuImages() != null && !request.getSpuImages().isEmpty()) {
            spu.setDefaultImg(getBestImageUrl(request.getSpuImages()));
        }

        if (request.getSkus() != null && !request.getSkus().isEmpty()) {
            List<BigDecimal> prices = request.getSkus().stream()
                    .map(PublishSkuDTO::getPrice)
                    .toList();

            spu.setMinPrice(Collections.min(prices));
            spu.setMaxPrice(Collections.max(prices));
        } else {
            spu.setMinPrice(BigDecimal.ZERO);
            spu.setMaxPrice(BigDecimal.ZERO);
        }

        spuMapper.insert(spu);

        if (request.getBasicAttrs() != null) {
            for (AttrValueDTO attr : request.getBasicAttrs()) {
                if (attrMapper.selectById(attr.getAttrId()) == null) {
                    throw new IllegalArgumentException("属性不存在: " + attr.getAttrId());
                }
                SpuAttrValue spuAttr = new SpuAttrValue();
                spuAttr.setSpuId(spu.getSpuId());
                spuAttr.setAttrId(attr.getAttrId());
                spuAttr.setAttrValue(attr.getAttrValue());
                spuAttr.setAttrName(getAttrName(attr.getAttrId()));
                spuAttrValueMapper.insert(spuAttr);
            }
        }

        saveImages(spu.getSpuId(), request.getSpuImages(), false);

        if (request.getSkus() != null) {
            for (PublishSkuDTO skuDto : request.getSkus()) {
                Sku sku = new Sku();
                sku.setSpuId(spu.getSpuId());
                sku.setName(skuDto.getName());
                sku.setTitle(skuDto.getTitle());
                sku.setSubtitle(skuDto.getSubtitle());
                sku.setPrice(skuDto.getPrice());
                sku.setStock(skuDto.getStock());
                sku.setStockLocked(0);
                sku.setSaleCount(0);
                sku.setCategoryId(spu.getCategoryId());
                sku.setBrandId(spu.getBrandId());
                sku.setCreateTime(LocalDateTime.now());
                sku.setUpdateTime(LocalDateTime.now());

                if (skuDto.getImages() != null && !skuDto.getImages().isEmpty()) {
                    sku.setDefaultImg(getBestImageUrl(skuDto.getImages()));
                }
                skuMapper.insert(sku);

                if (skuDto.getSaleAttrs() != null) {
                    for (AttrValueDTO attr : skuDto.getSaleAttrs()) {
                        if (attrMapper.selectById(attr.getAttrId()) == null) {
                            throw new IllegalArgumentException("属性不存在: " + attr.getAttrId());
                        }
                        SkuAttrValue skuAttr = new SkuAttrValue();
                        skuAttr.setSkuId(sku.getSkuId());
                        skuAttr.setAttrId(attr.getAttrId());
                        skuAttr.setAttrValue(attr.getAttrValue());
                        skuAttr.setAttrName(getAttrName(attr.getAttrId()));
                        skuAttrValueMapper.insert(skuAttr);
                    }
                }

                saveImages(sku.getSkuId(), skuDto.getImages(), true);
            }
        }

        clearProductCache(spu.getSpuId());
    }

    private String getBestImageUrl(List<ImageDTO> images) {
        return images.stream()
                .filter(img -> img.getUrl() != null && !img.getUrl().trim().isEmpty())
                .min(Comparator.comparingInt(img ->
                        img.getSort() != null ? (img.getSort() == 0 ? -1 : img.getSort()) : 999
                ))
                .map(img -> img.getUrl().trim())
                .orElse(null);
    }

    private void saveImages(Long entityId, List<ImageDTO> images, boolean isSku) {
        if (images == null) return;

        for (int i = 0; i < images.size(); i++) {
            ImageDTO img = images.get(i);
            if (img.getUrl() == null || img.getUrl().trim().isEmpty()) continue;

            if (isSku) {
                SkuImage skuImage = new SkuImage();
                skuImage.setSkuId(entityId);
                skuImage.setImgUrl(img.getUrl().trim());
                skuImage.setSort(img.getSort() != null ? img.getSort() : i);
                skuImageMapper.insert(skuImage);
            } else {
                SpuImage spuImage = new SpuImage();
                spuImage.setSpuId(entityId);
                spuImage.setImgUrl(img.getUrl().trim());
                spuImage.setSort(img.getSort() != null ? img.getSort() : i);
                spuImageMapper.insert(spuImage);
            }
        }
    }

    private String getAttrName(Long attrId) {
        Attr attr = attrMapper.selectById(attrId);
        return attr != null ? attr.getAttrName() : "未知";
    }

    @Override
    public void updateSaleCount(Long spuId, Integer quantity) {
        spuMapper.update(null,
                new UpdateWrapper<Spu>()
                        .setSql("sale_count = sale_count + " + quantity)
                        .eq("spu_id", spuId)
        );
    }

    @Override
    public Page<SpuSimpleDTO> getProductList(ProductListQuery query) {
        Page<Spu> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Spu> wrapper = new LambdaQueryWrapper<>();

        if (query.getCategoryId() != null) {
            wrapper.eq(Spu::getCategoryId, query.getCategoryId());
        }

        if (query.getBrandId() != null) {
            wrapper.eq(Spu::getBrandId, query.getBrandId());
        }

        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.like(Spu::getName, query.getKeyword());
        }

        if (query.getMinPrice() != null) {
            wrapper.ge(Spu::getMinPrice, query.getMinPrice());
        }
        if (query.getMaxPrice() != null) {
            wrapper.le(Spu::getMaxPrice, query.getMaxPrice());
        }

        String sort = query.getSort();
        if (sort == null) {
            sort = "default";
        }

        switch (sort) {
            case "price_asc":
                wrapper.orderByAsc(Spu::getMinPrice);
                break;
            case "price_desc":
                wrapper.orderByDesc(Spu::getMinPrice);
                break;
            case "sales_desc":
                wrapper.orderByDesc(Spu::getSaleCount);
                break;
            case "default":
            default:
                wrapper.orderByDesc(Spu::getUpdateTime);
        }

        IPage<Spu> result = this.page(page, wrapper);

        return (Page<SpuSimpleDTO>) result.convert(spu -> {
            SpuSimpleDTO dto = new SpuSimpleDTO();
            dto.setSpuId(spu.getSpuId());
            dto.setName(spu.getName());
            dto.setMinPrice(getMinPrice(spu.getSpuId()));
            dto.setDefaultImg(spu.getDefaultImg());
            dto.setSaleCount(spu.getSaleCount());
            dto.setCreateTime(spu.getCreateTime());
            return dto;
        });
    }

    private BigDecimal getMinPrice(Long spuId) {
        return skuMapper.selectList(
                        new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, spuId)
                ).stream()
                .map(Sku::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public Page<SpuDTO> getSpuList(SpuQueryDTO query) {
        Page<Spu> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Spu> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(query.getName())) {
            wrapper.like(Spu::getName, query.getName());
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(Spu::getCategoryId, query.getCategoryId());
        }
        if (query.getBrandId() != null) {
            wrapper.eq(Spu::getBrandId, query.getBrandId());
        }
        if (query.getPublishStatus() !=  null) {
            wrapper.eq(Spu::getPublishStatus, query.getPublishStatus());
        }

        wrapper.orderByDesc(Spu::getCreateTime);
        IPage<Spu> spuPage = spuMapper.selectPage(page, wrapper);

        List<Long> categoryIds = spuPage.getRecords().stream()
                .map(Spu::getCategoryId)
                .collect(Collectors.toList());
        List<Long> brandIds = spuPage.getRecords().stream()
                .map(Spu::getBrandId)
                .collect(Collectors.toList());

        Map<Long, String> categoryMap = getCategoryNameMap(categoryIds);
        Map<Long, String> brandMap = getBrandNameMap(brandIds);

        return (Page<SpuDTO>) spuPage.convert(spu -> {
            SpuDTO dto = new SpuDTO();
            dto.setSpuId(spu.getSpuId());
            dto.setName(spu.getName());
            dto.setDescription(spu.getDescription());
            dto.setCategoryId(spu.getCategoryId());
            dto.setBrandId(spu.getBrandId());
            dto.setPublishStatus(spu.getPublishStatus());
            dto.setSaleCount(spu.getSaleCount());
            dto.setDefaultImg(spu.getDefaultImg());
            dto.setCreateTime(spu.getCreateTime());
            dto.setCategoryName(categoryMap.get(spu.getCategoryId()));
            dto.setBrandName(brandMap.get(spu.getBrandId()));
            return dto;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpu(UpdateSpuDTO dto) {
        Spu existing = spuMapper.selectById(dto.getSpuId());
        if (existing == null) {
            throw new BusinessException("商品不存在");
        }

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPublishStatus(dto.getPublishStatus());
        existing.setUpdateTime(LocalDateTime.now());

        spuMapper.updateById(existing);

        clearProductCache(dto.getSpuId());
    }

    @Override
    public void updateSpuStatus(Long id, Integer publishStatus) {
        Spu spu = spuMapper.selectById(id);
        if (spu == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        spu.setPublishStatus(publishStatus);
        spuMapper.updateById(spu);

        if (publishStatus == 0) {
            skuMapper.update(null,
                    new LambdaUpdateWrapper<Sku>()
                            .set(Sku::getStatus, 0)
                            .eq(Sku::getSpuId, id)
            );
        }

        clearProductCache(id);
    }

    @Override
    public void deleteSpu(Long id) {
        Long skuCount = skuMapper.selectCount(
                new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, id)
        );
        if (skuCount > 0) {
            throw new IllegalArgumentException("存在 SKU，无法删除商品");
        }

        spuMapper.deleteById(id);
        clearProductCache(id);
    }

    @Override
    public String getSPUName(Long spuId) {
        if (spuId == null) {
            return null;
        }
        Spu spu = this.getById(spuId);
        return spu != null ? spu.getName() : null;
    }

    @Override
    public SpuDTO getSPUById(Long spuId) {
        if (spuId == null) {
            return null;
        }

        Spu spu = this.getById(spuId);
        if (spu == null) {
            return null;
        }

        SpuDTO dto = convertToDTO(spu);

        if (dto.getCategoryId() != null) {
            Category category = categoryMapper.selectById(dto.getCategoryId());
            if (category != null) {
                dto.setCategoryName(category.getName());
            }
        }

        if (dto.getBrandId() != null) {
            Brand brand = brandMapper.selectById(dto.getBrandId());
            if (brand != null) {
                dto.setBrandName(brand.getName());
            }
        }

        return dto;
    }

    private SpuDTO convertToDTO(Spu spu) {
        SpuDTO dto = new SpuDTO();
        dto.setSpuId(spu.getSpuId());
        dto.setName(spu.getName());
        dto.setDescription(spu.getDescription());
        dto.setCategoryId(spu.getCategoryId());
        dto.setBrandId(spu.getBrandId());
        dto.setPublishStatus(spu.getPublishStatus());
        dto.setSaleCount(spu.getSaleCount());
        dto.setDefaultImg(spu.getDefaultImg());
        dto.setCreateTime(spu.getCreateTime());
        dto.setUpdateTime(spu.getUpdateTime());
        return dto;
    }

    private void clearProductCache(Long spuId) {
        if (spuId != null && spuId > 0) {
            String cacheKey = PRODUCT_DETAIL_CACHE_KEY_PREFIX + spuId;
            redisTemplate.delete(cacheKey);
        }
    }

    private Map<Long, String> getCategoryNameMap(List<Long> ids) {
        if (ids.isEmpty()) return Collections.emptyMap();
        List<Category> categories = categoryMapper.selectBatchIds(ids);
        return categories.stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName));
    }

    private Map<Long, String> getBrandNameMap(List<Long> ids) {
        if (ids.isEmpty()) return Collections.emptyMap();
        List<Brand> brands = brandMapper.selectBatchIds(ids);
        return brands.stream()
                .collect(Collectors.toMap(Brand::getId, Brand::getName));
    }
}
