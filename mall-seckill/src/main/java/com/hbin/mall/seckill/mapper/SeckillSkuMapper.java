package com.hbin.mall.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbin.mall.seckill.domain.SeckillSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author hbin
 * @since 2026-01-07
 */
@Mapper
public interface SeckillSkuMapper extends BaseMapper<SeckillSku> {

    @Select("SELECT * FROM seckill_sku WHERE activity_id = #{activityId}")
    List<SeckillSku> selectByActivityId(@Param("activityId") Long activityId);
}
