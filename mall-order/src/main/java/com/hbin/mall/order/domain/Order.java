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

@Getter
@Setter
@ToString
@TableName("`order`")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 会员用户名
     */
    private String username;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 运费金额
     */
    private BigDecimal freightAmount;

    /**
     * 优惠金额
     */
    private BigDecimal promotionAmount;

    /**
     * 应付金额
     */
    private BigDecimal payAmount;

    /**
     * 订单状态（0-待付款，1-已付款，2-已发货，3-已完成，4-已关闭，5-已取消）
     */
    private Integer status;

    /**
     * 支付方式（1-支付宝，2-微信，3-银行卡）
     */
    private Integer payType;

    /**
     * 提交时间
     */
    private LocalDateTime createTime;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 收货时间
     */
    private LocalDateTime receiveTime;

    /**
     * 关闭时间
     */
    private LocalDateTime closeTime;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 收货人邮编
     */
    private String receiverPostCode;

    /**
     * 省份/直辖市
     */
    private String receiverProvince;

    /**
     * 城市
     */
    private String receiverCity;

    /**
     * 区
     */
    private String receiverRegion;

    /**
     * 详细地址
     */
    private String receiverDetailAddress;

    /**
     * 自动确认时间（天）
     */
    private Integer autoConfirmDay;

    /**
     * 订单备注
     */
    private String note;

    /**
     * 使用的优惠券码
     */
    private String couponCode;

    /**
     * 优惠券减免金额
     */
    private BigDecimal couponDiscount;
}
