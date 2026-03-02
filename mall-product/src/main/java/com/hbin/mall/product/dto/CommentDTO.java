package com.hbin.mall.product.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentDTO {
    private Long commentId;
    private String content;
    private Long memberId;
    private String memberUsername;
    private String memberAvatar;
    private Long toCommentId;
    private Long toCommentUserId;
    private String toMemberUsername;
    private Long rootId;
    private LocalDateTime createTime;
    private List<CommentDTO> children; // 子评论
}
