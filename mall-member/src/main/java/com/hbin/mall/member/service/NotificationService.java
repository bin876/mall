package com.hbin.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.common.event.NotificationEvent;
import com.hbin.mall.member.domain.Notification;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationService extends IService<Notification> {

    void sendNotificationEvent(NotificationEvent event);

    @Transactional
    void saveNotification(Notification notification);

    @Transactional
    void markAsRead(Long notificationId, Long memberId);
}
