package com.hbin.mall.product.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class Banner implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 轮播图ID
     */
    @TableId(value = "banner_id", type = IdType.AUTO)
    private Long bannerId;

    /**
     * 标题（可选）
     */
    private String title;

    /**
     * 图片URL（MinIO路径）
     */
    private String imageUrl;

    /**
     * 跳转链接（商品详情/活动页等）
     */
    private String targetUrl;

    /**
     * 排序值（越小越靠前）
     */
    private Integer sort;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
