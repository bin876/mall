package com.hbin.mall.product.util;

import com.hbin.mall.product.domain.Category;
import com.hbin.mall.product.mapper.CategoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class CategoryPathUtil {

    @Autowired
    private CategoryMapper categoryMapper;

    private final Map<Long, String> categoryPathCache = new ConcurrentHashMap<>();

    public String getCategoryPath(Long thirdLevelCategoryId) {
        if (thirdLevelCategoryId == null) return null;
        
        return categoryPathCache.computeIfAbsent(thirdLevelCategoryId, id -> {
            try {
                Category third = categoryMapper.selectById(id);
                if (third == null || third.getParentId() == 0) {
                    return String.valueOf(id);
                }
                
                Category second = categoryMapper.selectById(third.getParentId());
                if (second == null || second.getParentId() == 0) {
                    return third.getParentId() + "_" + id;
                }
                
                return second.getParentId() + "_" + second.getCategoryId() + "_" + id;
            } catch (Exception e) {
                log.warn("获取分类路径失败, categoryId={}", id, e);
                return String.valueOf(id);
            }
        });
    }

    public void clearCache() {
        categoryPathCache.clear();
    }
}