package com.hbin.mall.order.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户优惠券
 * </p>
 *
 * @author hbin
 * @since 2026-01-10
 */
@Getter
@Setter
@ToString
@TableName("user_coupon")
public class UserCoupon implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "coupon_id", type = IdType.AUTO)
    private Long couponId;

    private Long templateId;

    private Long memberId;

    /**
     * 唯一券码
     */
    private String couponCode;

    /**
     * 0-未使用, 1-已使用, 2-已过期
     */
    private Integer status;

    private LocalDateTime receiveTime;

    private LocalDateTime useTime;

    /**
     * 使用订单号
     */
    private String orderSn;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
}
