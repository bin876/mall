package com.hbin.mall.member.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.common.satoken.StpMemberUtil;
import com.hbin.mall.member.domain.Notification;
import com.hbin.mall.member.dto.NotificationStats;
import com.hbin.mall.member.mapper.NotificationMapper;
import com.hbin.mall.member.service.NotificationService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/member/notification")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private NotificationMapper notificationMapper;
    
    // 获取未读通知数量
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        Long count = notificationMapper.selectCount(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getMemberId, memberId)
                .eq(Notification::getReadStatus, 0)
        );
        return Result.success(count);
    }

    @GetMapping("/list")
    public Result<IPage<Notification>> getNotificationList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(value = "readStatus", required = false) Integer readStatus) {

        Long memberId = StpMemberUtil.getLoginIdAsLong();
        Page<Notification> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getMemberId, memberId);

        if (readStatus != null) {
            wrapper.eq(Notification::getReadStatus, readStatus);
        }

        wrapper.orderByDesc(Notification::getCreateTime);
        IPage<Notification> result = notificationMapper.selectPage(page, wrapper);

        return Result.success(result);
    }
    
    // 标记为已读
    @PostMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        notificationService.markAsRead(id, memberId);
        return Result.success();
    }
    
    // 标记全部已读
    @PostMapping("/read-all")
    public Result<Void> markAllAsRead() {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        notificationMapper.update(null,
            new LambdaUpdateWrapper<Notification>()
                .set(Notification::getReadStatus, 1)
                .set(Notification::getReadTime, LocalDateTime.now())
                .eq(Notification::getMemberId, memberId)
                .eq(Notification::getReadStatus, 0)
        );
        return Result.success();
    }

    @GetMapping("/stats")
    public Result<NotificationStats> getNotificationStats() {
        Long memberId = StpMemberUtil.getLoginIdAsLong();

        Long totalCount = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>().eq(Notification::getMemberId, memberId)
        );

        Long unreadCount = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getMemberId, memberId)
                        .eq(Notification::getReadStatus, 0)
        );

        NotificationStats stats = new NotificationStats();
        stats.setTotalCount(totalCount);
        stats.setUnreadCount(unreadCount);
        stats.setReadCount(totalCount - unreadCount);

        return Result.success(stats);
    }
}