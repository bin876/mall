// CommentController.java
package com.hbin.mall.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.common.satoken.StpMemberUtil;
import com.hbin.mall.product.dto.CommentCreateDTO;
import com.hbin.mall.product.dto.CommentDTO;
import com.hbin.mall.product.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/list")
    public Result<Page<CommentDTO>> getCommentList(
            @RequestParam Long spuId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<CommentDTO> page = commentService.getCommentList(spuId, pageNum, pageSize);
        return Result.success(page);
    }

    @PostMapping
    public Result<String> createComment(@Valid @RequestBody CommentCreateDTO dto) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();

        commentService.createComment(memberId, dto);
        return Result.success("评论成功");
    }

    @DeleteMapping("/{commentId}")
    public Result<String> deleteComment(@PathVariable Long commentId) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();

        commentService.deleteComment(memberId, commentId);
        return Result.success("删除成功");
    }
}