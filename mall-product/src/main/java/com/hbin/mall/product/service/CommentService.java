package com.hbin.mall.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.product.domain.Comment;
import com.hbin.mall.product.dto.CommentCreateDTO;
import com.hbin.mall.product.dto.CommentDTO;
import org.springframework.transaction.annotation.Transactional;

public interface CommentService extends IService<Comment> {

    Page<CommentDTO> getCommentList(Long spuId, Integer pageNum, Integer pageSize);

    @Transactional
    void createComment(Long memberId, CommentCreateDTO dto);

    @Transactional
    void deleteComment(Long memberId, Long commentId);
}
