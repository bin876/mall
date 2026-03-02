package com.hbin.mall.product.dto;

import lombok.Data;

@Data
public class CommentCreateDTO {
    private Long spuId;
    private String content;
    private Long toCommentId;      // 回复目标评论ID
    private Long toCommentUserId;  // 回复目标用户ID
}