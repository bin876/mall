package com.hbin.mall.product.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
@TableName("spu_image")
public class SpuImage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "spu_image_id", type = IdType.AUTO)
    private Long spuImageId;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * 图片URL
     */
    private String imgUrl;

    /**
     * 图片排序
     */
    private Integer sort;

    /**
     * 默认图（0-否，1-是）
     */
    private Integer isDefault;
}
