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
public class Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "cart_id", type = IdType.AUTO)
    private Long cartId;

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * SKU名称
     */
    private String skuName;

    /**
     * 添加到购物车时的价格
     */
    private BigDecimal skuPrice;

    /**
     * SKU主图
     */
    private String skuPic;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 是否选中（1=选中，0=未选）
     */
    private Integer checked;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
