package com.hbin.mall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.order.domain.CouponTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface CouponTemplateMapper extends BaseMapper<CouponTemplate> {
    /**
     * 分页查询优惠券模板
     */
    @Select("<script>" +
            "SELECT * FROM coupon_template " +
            "<where>" +
            "  <if test='name != null and name != \"\"'>" +
            "    AND name LIKE CONCAT('%', #{name}, '%')" +
            "  </if>" +
            "  <if test='couponType != null'>" +
            "    AND coupon_type = #{couponType}" +
            "  </if>" +
            "  <if test='status != null'>" +
            "    AND status = #{status}" +
            "  </if>" +
            "  <if test='startTime != null'>" +
            "    AND create_time >= #{startTime}" +
            "  </if>" +
            "  <if test='endTime != null'>" +
            "    AND create_time &lt;= #{endTime}" +
            "  </if>" +
            "</where>" +
            "ORDER BY create_time DESC" +
            "</script>")
    IPage<CouponTemplate> selectPageWithCondition(Page<?> page,
                                                  @Param("name") String name,
                                                  @Param("couponType") Integer couponType,
                                                  @Param("status") Integer status,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 统计已发放数量
     */
    @Select("SELECT COUNT(*) FROM user_coupon WHERE template_id = #{templateId}")
    int countIssuedByTemplateId(@Param("templateId") Long templateId);
}
