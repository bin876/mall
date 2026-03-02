package com.hbin.mall.member.component;

import com.alibaba.fastjson.JSON;
import com.hbin.mall.member.domain.Notification;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/notifications/{memberId}")
@Slf4j
public class NotificationWebSocket {
    
    private static final Map<Long, Set<Session>> userSessions = new ConcurrentHashMap<>();
    
    @OnOpen
    public void onOpen(@PathParam("memberId") Long memberId, Session session) {
        userSessions.computeIfAbsent(memberId, k -> new HashSet<>()).add(session);
        log.info("用户 {} 连接通知WebSocket", memberId);
    }
    
    @OnClose
    public void onClose(@PathParam("memberId") Long memberId, Session session) {
        Set<Session> sessions = userSessions.get(memberId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSessions.remove(memberId);
            }
        }
        log.info("用户 {} 断开通知WebSocket", memberId);
    }
    
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket错误", error);
    }
    
    public static void sendNotification(Long memberId, Notification notification) {
        Set<Session> sessions = userSessions.get(memberId);
        if (sessions != null) {
            sessions.forEach(session -> {
                if (session.isOpen()) {
                    try {
                        String json = JSON.toJSONString(notification);
                        session.getBasicRemote().sendText(json);
                    } catch (IOException e) {
                        log.error("发送通知失败", e);
                    }
                }
            });
        }
    }
}