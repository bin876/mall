package com.hbin.mall.member.mq.consumer;

import com.hbin.mall.common.event.NotificationEvent;
import com.hbin.mall.member.component.NotificationWebSocket;
import com.hbin.mall.member.config.RabbitMQConfig;
import com.hbin.mall.member.domain.Notification;
import com.hbin.mall.member.mapper.NotificationMapper;
import com.hbin.mall.member.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class NotificationConsumer {
    
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationMapper notificationMapper;

    @RabbitListener(queues = "notification.queue")
    public void handleNotification(NotificationEvent event) {
        try {
            boolean exists = notificationMapper.existsByUniqueKey(
                    event.getMemberId(),
                    event.getRelatedId(),
                    event.getType(),
                    event.getCategory()
            );

            if (exists) {
                log.debug("重复通知，跳过处理: memberId={}, relatedId={}, type={}",
                        event.getMemberId(), event.getRelatedId(), event.getType());
                return;
            }

            Notification notification = new Notification();
            notification.setMemberId(event.getMemberId());
            notification.setTitle(event.getTitle());
            notification.setContent(event.getContent());
            notification.setType(event.getType());
            notification.setCategory(event.getCategory());
            notification.setTargetUrl(event.getTargetUrl());
            notification.setRelatedId(event.getRelatedId());
            notification.setCreateTime(LocalDateTime.now());

            notificationService.saveNotification(notification);
            NotificationWebSocket.sendNotification(event.getMemberId(), notification);

            log.info("通知创建成功: id={}", notification.getNotificationId());

        } catch (Exception e) {
            log.error("处理通知失败", e);
            throw new RuntimeException(e);
        }
    }
}