package com.hbin.mall.member.domain;

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
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "notification_id", type = IdType.AUTO)
    private Long notificationId;

    /**
     * 接收用户ID
     */
    private Long memberId;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 类型：1-系统, 2-营销, 3-互动
     */
    private Integer type;

    /**
     * 分类：order/p_coupon/security/marketing
     */
    private String category;

    /**
     * 0-未读, 1-已读
     */
    private Integer readStatus;

    /**
     * 跳转链接
     */
    private String targetUrl;

    /**
     * 关联ID（订单ID/商品ID等）
     */
    private Long relatedId;

    private LocalDateTime createTime;

    private LocalDateTime readTime;
}
