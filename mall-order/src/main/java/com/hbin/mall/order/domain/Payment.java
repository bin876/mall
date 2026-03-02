package com.hbin.mall.order.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "payment_id", type = IdType.AUTO)
    private Long paymentId;

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 支付流水号
     */
    private String paySn;

    /**
     * 支付方式（1-支付宝，2-微信，3-银行卡）
     */
    private Integer payType;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付状态（0-待支付，1-成功，2-失败，3-已退款）
     */
    private Integer status;

    /**
     * 支付回调原始内容
     */
    private String callbackContent;

    /**
     * 支付成功时间
     */
    private LocalDateTime payTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
