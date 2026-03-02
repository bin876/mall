package com.hbin.mall.order.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 优惠券模板
 * </p>
 *
 * @author hbin
 * @since 2026-01-10
 */
@Getter
@Setter
@ToString
@TableName("coupon_template")
public class CouponTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "template_id", type = IdType.AUTO)
    private Long templateId;

    /**
     * 优惠券名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 类型：1-满减, 2-折扣, 3-无门槛
     */
    private Integer couponType;

    /**
     * 满减金额/折扣比例
     */
    private BigDecimal discountAmount;

    /**
     * 最低订单金额
     */
    private BigDecimal minOrderAmount;

    /**
     * 总发行量(-1=不限)
     */
    private Integer totalCount;

    /**
     * 每人限领
     */
    private Integer perUserLimit;

    /**
     * 领取后有效天数
     */
    private Integer validDays;

    /**
     * 有效期开始
     */
    private LocalDateTime startTime;

    /**
     * 有效期结束
     */
    private LocalDateTime endTime;

    /**
     * 状态：0-禁用, 1-启用
     */
    private Integer status;

    private LocalDateTime createTime;
}
