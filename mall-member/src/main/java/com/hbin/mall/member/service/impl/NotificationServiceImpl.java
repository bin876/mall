package com.hbin.mall.member.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.common.event.NotificationEvent;
import com.hbin.mall.member.config.RabbitMQConfig;
import com.hbin.mall.member.domain.Notification;
import com.hbin.mall.member.mapper.NotificationMapper;
import com.hbin.mall.member.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void sendNotificationEvent(NotificationEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                event
        );
    }

    @Override
    @Transactional
    public void saveNotification(Notification notification) {
        notificationMapper.insert(notification);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long memberId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification != null && notification.getMemberId().equals(memberId)) {
            notification.setReadStatus(1);
            notification.setReadTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
    }
}
