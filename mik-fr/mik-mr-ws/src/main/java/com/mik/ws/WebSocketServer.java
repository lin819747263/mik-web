package com.mik.ws;

import com.mik.ws.config.CustomConfigurator;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@ServerEndpoint(value = "/websocket/{userId}", configurator = CustomConfigurator.class, encoders = {ServerEncoder.class})
public class WebSocketServer {
    private static final  ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    private static final AtomicInteger onlineCount = new AtomicInteger(0);

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        // 身份验证
        if (!authenticate(session)) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Authentication failed"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        sessions.put(userId, session);
        // 发送连接成功消息
        sendMessage(session, "Connected successfully");
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 处理接收到的消息
        handleMessage(message, session);
        WsMessage wsMessage = WsMessage.builder().msgId("1001").msgType("recv").data("hello" + System.currentTimeMillis()).build();
        session.getAsyncRemote().sendObject(wsMessage);
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        log.info("连接已断开");
        sessions.remove(userId);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.info("发生错误");
        throwable.printStackTrace();
    }

    // 单播消息
    public void sendToUser(String userId, String message) {
        Session session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
        }
    }

    // 多播消息
    public void sendToUsers(List<String> userIds, String message) {
        for (String userId : userIds) {
            sendToUser(userId, message);
        }
    }

    // 广播消息
    public void broadcast(String message) {
        sessions.values().forEach(session -> sendMessage(session, message));
    }

    private void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean authenticate(Session session) {
        // 实现身份验证逻辑，例如检查JWT token
        String token = (String) session.getUserProperties().get("token");
        return JwtUtil.validateToken(token);
    }

    private void handleMessage(String message, Session session) {
        System.out.println(message);
    }
}
