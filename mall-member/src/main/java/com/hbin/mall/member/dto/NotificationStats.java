package com.hbin.mall.member.dto;

import lombok.Data;

@Data
public class NotificationStats {
    private Long totalCount;
    private Long unreadCount;
    private Long readCount;
}