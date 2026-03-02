package com.hbin.mall.product.domain;

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
@TableName("product_collect")
public class ProductCollect implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收藏ID
     */
    @TableId(value = "collect_id", type = IdType.AUTO)
    private Long collectId;

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 商品SPU ID
     */
    private Long spuId;

    /**
     * 商品名称（冗余）
     */
    private String spuName;

    /**
     * 商品图片（冗余）
     */
    private String spuPic;

    /**
     * 商品价格（冗余）
     */
    private BigDecimal spuPrice;

    /**
     * 收藏时间
     */
    private LocalDateTime createTime;
}
