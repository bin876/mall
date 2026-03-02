package com.hbin.mall.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.product.domain.SpuDocument;
import com.hbin.mall.product.dto.ProductListQuery;
import com.hbin.mall.product.dto.SpuSimpleDTO;
import com.hbin.mall.product.util.CategoryPathUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SearchService {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private CategoryPathUtil categoryPathUtil;

    public IPage<SpuSimpleDTO> searchProducts(ProductListQuery query) {
        String keyword = query.getKeyword();
        if (keyword == null || keyword.trim().isEmpty()) {
            return new Page<SpuSimpleDTO>()
                    .setCurrent(query.getPageNum())
                    .setSize(query.getPageSize())
                    .setRecords(Collections.emptyList())
                    .setTotal(0);
        }

        try {
            Criteria criteria = buildKeywordCriteria(keyword.trim());

            if (query.getCategoryId() != null) {
                criteria = criteria.and(new Criteria("categoryId").is(query.getCategoryId()));
            }
            if (query.getBrandId() != null) {
                criteria = criteria.and(new Criteria("brandId").is(query.getBrandId()));
            }
            if (query.getMinPrice() != null) {
                criteria = criteria.and(new Criteria("minPrice").greaterThanEqual(
                        query.getMinPrice().doubleValue()));
            }
            if (query.getMaxPrice() != null) {
                criteria = criteria.and(new Criteria("maxPrice").lessThanEqual(
                        query.getMaxPrice().doubleValue()));
            }

            Pageable pageable = PageRequest.of(
                    Math.max(0, query.getPageNum() - 1),
                    query.getPageSize()
            );
            Sort sort = buildSort(query.getSort());

            CriteriaQuery searchQuery = new CriteriaQuery(criteria)
                    .setPageable(pageable)
                    .addSort(sort);

            SearchHits<SpuDocument> searchHits = elasticsearchOperations.search(searchQuery, SpuDocument.class);

            List<SpuSimpleDTO> dtoList = searchHits.getSearchHits().stream()
                    .map(hit -> convertToDto(hit.getContent()))
                    .collect(Collectors.toList());

            return new Page<SpuSimpleDTO>()
                    .setCurrent(query.getPageNum())
                    .setSize(query.getPageSize())
                    .setRecords(dtoList)
                    .setTotal(searchHits.getTotalHits());

        } catch (Exception e) {
            log.error("ES搜索失败", e);
            throw new RuntimeException("搜索失败", e);
        }
    }

    private SpuSimpleDTO convertToDto(SpuDocument doc) {
        SpuSimpleDTO dto = new SpuSimpleDTO();
        dto.setSpuId(doc.getSpuId());
        dto.setName(doc.getName());
        dto.setMinPrice(BigDecimal.valueOf(doc.getMinPrice()));
        dto.setMaxPrice(BigDecimal.valueOf(doc.getMaxPrice()));
        dto.setSaleCount(doc.getSaleCount());
        dto.setDefaultImg(doc.getDefaultImg());

        if (doc.getCreateTime() != null) {
            LocalDateTime createTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(doc.getCreateTime()),
                    ZoneId.systemDefault()
            );
            dto.setCreateTime(createTime);
        }
        return dto;
    }

    private Criteria buildKeywordCriteria(String keyword) {
        return new Criteria("name").matches(keyword)
            .or(new Criteria("brandName").matches(keyword));
    }

    private Sort buildSort(String sort) {
        if (sort == null) return Sort.unsorted();
        
        return switch (sort) {
            case "sales_desc" -> Sort.by(Sort.Direction.DESC, "saleCount");
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "minPrice");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "minPrice");
            default -> Sort.unsorted();
        };
    }
}