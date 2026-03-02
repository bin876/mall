package com.hbin.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbin.mall.product.domain.CategoryBrand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryBrandMapper extends BaseMapper<CategoryBrand> {

    int insertBatchSomeColumn(@Param("list") List<CategoryBrand> batchList);
}
