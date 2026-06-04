package com.study.room.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@ServerEndpoint("/ws/{userId}")
public class WebSocketServer {

    // 记录在线的 WebSocket 连接总数
    private static final AtomicInteger onlineCount = new AtomicInteger(0);

    // 用并发安全的 Map 存放每个 userId 的 WebSocket 会话 (Session)
    // 以便在后台能够根据 userId 找到对应的 Socket 连接进行定点主动推送
    private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        sessionMap.put(userId, session);
        onlineCount.incrementAndGet();
        log.info("有新连接加入！当前在线人数为: {}。加入的 userId = {}", onlineCount.get(), userId);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        if (sessionMap.containsKey(userId)) {
            sessionMap.remove(userId);
            onlineCount.decrementAndGet();
            log.info("有一连接关闭！当前在线人数为: {}。退出的 userId = {}", onlineCount.get(), userId);
        }
    }

    /**
     * 收到客户端消息后调用的方法
     * （在本场景中主要是单向推送，一般用不到）
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        log.info("收到来自 userId={}, 的消息: {}", userId, message);
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error, @PathParam("userId") String userId) {
        log.error("发生错误：userId={}", userId, error);
    }

    /**
     * 核心方法：提供给后台按 userId 点对点推送消息
     * 比如“自习室只剩 10 分钟，即将到期是否续费”
     * @param userId 目标用户 ID
     * @param message 需推送的消息文本
     */
    public static void sendMessageToUser(String userId, String message) {
        Session activeSession = sessionMap.get(userId);
        if (activeSession != null && activeSession.isOpen()) {
            try {
                // 采用异步或同步发送方式都可以（这里演示的是同步）
                activeSession.getBasicRemote().sendText(message);
                log.info("【WebSocket服务端】给 userId={} 成功推送消息: {}", userId, message);
            } catch (IOException e) {
                log.error("给 userId={} 发送消息异常", userId, e);
            }
        } else {
            log.warn("发送失败，userId={} 均已下线或不存在！推送丢弃：{}", userId, message);
        }
    }
}
