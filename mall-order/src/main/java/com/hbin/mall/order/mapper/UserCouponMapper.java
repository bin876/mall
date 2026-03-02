package com.hbin.mall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.order.dto.UserCouponDTO;
import com.hbin.mall.order.domain.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    @Select("SELECT COUNT(*) FROM user_coupon WHERE member_id = #{memberId} AND template_id = #{templateId}")
    Integer countByMemberAndTemplate(@Param("memberId") Long memberId, @Param("templateId") Long templateId);

    @Select("        SELECT uc.*\n" +
            "        FROM user_coupon uc\n" +
            "        JOIN coupon_template ct ON uc.template_id = ct.template_id\n" +
            "        WHERE uc.member_id = #{memberId}\n" +
            "        AND uc.status = 0\n" +
            "        AND uc.expire_time > NOW()\n" +
            "        AND (ct.min_order_amount IS NULL OR ct.min_order_amount <= #{amount})")
    List<UserCoupon> selectAvailableCoupons(@Param("memberId") Long memberId, @Param("amount") BigDecimal amount);


    /**
     * 统计用户对该模板的领取数量
     */
    // @Select("SELECT COUNT(*) FROM user_coupon WHERE member_id = #{memberId} AND template_id = #{templateId}")
    // int countByMemberAndTemplate(@Param("memberId") Long memberId,
    //                              @Param("templateId") Long templateId);

    /**
     * 统计模板已发放数量
     */
    @Select("SELECT COUNT(*) FROM user_coupon WHERE template_id = #{templateId}")
    int countByTemplateId(@Param("templateId") Long templateId);

    /**
     * 批量插入用户优惠券
     */
    @Select("<script>" +
            "INSERT INTO user_coupon(template_id, member_id, coupon_code, status, receive_time, use_time, order_sn, expire_time) " +
            "VALUES " +
            "<foreach collection='coupons' item='coupon' separator=','>" +
            "(#{coupon.templateId}, #{coupon.memberId}, #{coupon.couponCode}, #{coupon.status}, " +
            "#{coupon.receiveTime}, #{coupon.useTime}, #{coupon.orderSn}, #{coupon.expireTime})" +
            "</foreach>" +
            "</script>")
    void batchInsert(@Param("coupons") List<UserCoupon> coupons);

    /**
     * 更新过期优惠券状态
     */
    @Update("UPDATE user_coupon SET status = 2 WHERE status = 0 AND expire_time <![CDATA[<]]> #{currentTime}")
    int updateExpiredCoupons(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 统计用户已使用的优惠券
     */
    @Select("SELECT COUNT(*) FROM user_coupon WHERE member_id = #{memberId} AND status = 1")
    int countUsedByMemberId(@Param("memberId") Long memberId);

    /**
     * 统计用户可用的优惠券
     */
    @Select("SELECT COUNT(*) FROM user_coupon WHERE member_id = #{memberId} AND status = 0 AND expire_time > #{currentTime}")
    int countAvailableByMemberId(@Param("memberId") Long memberId,
                                 @Param("currentTime") LocalDateTime currentTime);

    /**
     * 统计用户总优惠券
     */
    @Select("SELECT COUNT(*) FROM user_coupon WHERE member_id = #{memberId}")
    int countByMemberId(@Param("memberId") Long memberId);

    /**
     * 按类型统计用户优惠券
     */
    @Select("SELECT ct.coupon_type AS couponType, COUNT(*) AS count FROM user_coupon uc " +
            "JOIN coupon_template ct ON uc.template_id = ct.template_id " +
            "WHERE uc.member_id = #{memberId} " +
            "GROUP BY ct.coupon_type")
    List<Map<String, Object>> countByMemberAndType(@Param("memberId") Long memberId);

    /**
     * 获取用户优惠券分页
     */
    @Select("<script>" +
            "SELECT uc.*, ct.name AS templateName, ct.coupon_type AS couponType, " +
            "ct.discount_amount AS discountAmount, ct.min_order_amount AS minOrderAmount " +
            "FROM user_coupon uc " +
            "JOIN coupon_template ct ON uc.template_id = ct.template_id " +
            "<where>" +
            "  uc.member_id = #{memberId}" +
            "  <if test='status != null'>" +
            "    AND uc.status = #{status}" +
            "  </if>" +
            "  <if test='startTime != null'>" +
            "    AND uc.receive_time >= #{startTime}" +
            "  </if>" +
            "  <if test='endTime != null'>" +
            "    AND uc.receive_time &lt;= #{endTime}" +
            "  </if>" +
            "</where>" +
            "ORDER BY uc.receive_time DESC" +
            "</script>")
    IPage<UserCouponDTO> selectUserCouponsPage(Page<?> page,
                                               @Param("memberId") Long memberId,
                                               @Param("status") Integer status,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);
}
