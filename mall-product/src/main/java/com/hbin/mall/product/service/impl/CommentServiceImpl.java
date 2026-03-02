package com.hbin.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.api.member.feign.MemberFeignClient;
import com.hbin.mall.common.exception.BusinessException;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.product.domain.Comment;
import com.hbin.mall.product.dto.CommentCreateDTO;
import com.hbin.mall.product.dto.CommentDTO;
import com.hbin.mall.product.mapper.CommentMapper;
import com.hbin.mall.product.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private MemberFeignClient memberFeignClient;

    @Override
    public Page<CommentDTO> getCommentList(Long spuId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Comment> rootWrapper = new LambdaQueryWrapper<>();
        rootWrapper.eq(Comment::getSpuId, spuId)
                .eq(Comment::getType, 0)
                .isNull(Comment::getToCommentId)
                .orderByDesc(Comment::getCreateTime);

        Page<Comment> rootPage = new Page<>(pageNum, pageSize);
        IPage<Comment> rootComments = commentMapper.selectPage(rootPage, rootWrapper);

        if (rootComments.getRecords().isEmpty()) {
            return new Page<CommentDTO>()
                    .setCurrent(pageNum)
                    .setSize(pageSize)
                    .setTotal(0)
                    .setRecords(Collections.emptyList());
        }

        List<Long> rootIds = rootComments.getRecords().stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        Map<Long, List<Comment>> childrenMap = new HashMap<>();
        if (!rootIds.isEmpty()) {
            LambdaQueryWrapper<Comment> childrenWrapper = new LambdaQueryWrapper<>();
            childrenWrapper.eq(Comment::getSpuId, spuId)
                    .eq(Comment::getType, 0)
                    .in(Comment::getRootId, rootIds)
                    .isNotNull(Comment::getToCommentId)
                    .orderByAsc(Comment::getCreateTime);

            List<Comment> allChildren = commentMapper.selectList(childrenWrapper);

            for (Comment child : allChildren) {
                childrenMap.computeIfAbsent(child.getRootId(), k -> new ArrayList<>()).add(child);
            }
        }

        Set<Long> allMemberIds = new HashSet<>();
        rootComments.getRecords().forEach(comment -> allMemberIds.add(comment.getCreateBy()));

        for (List<Comment> children : childrenMap.values()) {
            children.forEach(comment -> {
                allMemberIds.add(comment.getCreateBy());
                if (comment.getToCommentUserId() != null) {
                    allMemberIds.add(comment.getToCommentUserId());
                }
            });
        }

        Map<Long, String> usernameMap = new HashMap<>();
        Map<Long, String> avatarMap = new HashMap<>();

        if (!allMemberIds.isEmpty()) {
            try {
                List<Long> memberIds = new ArrayList<>(allMemberIds);
                Result<Map<Long, String>> usernameResult = memberFeignClient.getMemberUsernames(memberIds);
                Result<Map<Long, String>> avatarResult = memberFeignClient.getMemberAvatarUrls(memberIds);

                if (usernameResult != null && usernameResult.getData() != null) {
                    usernameMap = usernameResult.getData();
                }
                if (avatarResult != null && avatarResult.getData() != null) {
                    avatarMap = avatarResult.getData();
                }
            } catch (Exception ignored) {
            }
        }

        List<CommentDTO> result = new ArrayList<>();
        for (Comment root : rootComments.getRecords()) {
            CommentDTO rootDto = convertToDTO(root, usernameMap, avatarMap);

            Map<Long, String> finalUsernameMap = usernameMap;
            Map<Long, String> finalAvatarMap = avatarMap;
            List<CommentDTO> childrenDtos = childrenMap.getOrDefault(root.getId(), new ArrayList<>())
                    .stream()
                    .map(child -> convertToDTO(child, finalUsernameMap, finalAvatarMap))
                    .collect(Collectors.toList());

            rootDto.setChildren(childrenDtos);
            result.add(rootDto);
        }

        return new Page<CommentDTO>()
                .setCurrent(pageNum)
                .setSize(pageSize)
                .setTotal(rootComments.getTotal())
                .setRecords(result);
    }

    @Override
    @Transactional
    public void createComment(Long memberId, CommentCreateDTO dto) {
        Comment comment = new Comment();
        comment.setType(0);
        comment.setSpuId(dto.getSpuId());
        comment.setContent(dto.getContent());
        comment.setCreateBy(memberId);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateBy(memberId);
        comment.setUpdateTime(LocalDateTime.now());
        comment.setStatus(1);

        if (dto.getToCommentId() == null) {
            comment.setRootId(0L);
            comment.setToCommentId(null);
            comment.setToCommentUserId(null);

            commentMapper.insert(comment);
            comment.setRootId(comment.getId());
            commentMapper.updateById(comment);
        } else {
            Comment targetComment = commentMapper.selectById(dto.getToCommentId());
            if (targetComment == null) {
                throw new BusinessException("目标评论不存在");
            }

            comment.setToCommentId(dto.getToCommentId());
            comment.setToCommentUserId(dto.getToCommentUserId());
            comment.setRootId(targetComment.getRootId());

            commentMapper.insert(comment);
        }
    }

    @Override
    @Transactional
    public void deleteComment(Long memberId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        if (!memberId.equals(comment.getCreateBy())) {
            throw new BusinessException("无权删除他人评论");
        }

        deleteCommentTree(commentId);
    }

    private void deleteCommentTree(Long commentId) {
        List<Comment> children = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>().eq(Comment::getToCommentId, commentId)
        );

        for (Comment child : children) {
            deleteCommentTree(child.getId());
        }

        commentMapper.deleteById(commentId);
    }

    private CommentDTO convertToDTO(Comment comment, Map<Long, String> usernameMap, Map<Long, String> avatarMap) {
        CommentDTO dto = new CommentDTO();
        dto.setCommentId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setMemberId(comment.getCreateBy());
        dto.setMemberUsername(usernameMap.getOrDefault(comment.getCreateBy(), "匿名用户"));
        dto.setMemberAvatar(avatarMap.getOrDefault(comment.getCreateBy(), "/default-avatar.png"));
        dto.setToCommentId(comment.getToCommentId());
        dto.setToCommentUserId(comment.getToCommentUserId());

        if (comment.getToCommentUserId() != null) {
            dto.setToMemberUsername(usernameMap.getOrDefault(comment.getToCommentUserId(), "用户"));
        }

        dto.setRootId(comment.getRootId());
        dto.setCreateTime(comment.getCreateTime());
        return dto;
    }
}
