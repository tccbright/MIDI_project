// src/main/java/com/tccb/virtualband/controller/StatusWebSocketHandler.java
package com.tccb.virtualband.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tccb.virtualband.entity.InstrumentStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StatusWebSocketHandler extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("WS 已连接，会话数=" + sessions.size());

        // 🔥 新增：建立连接时就推一条测试消息
        try {
            session.sendMessage(new TextMessage("{\"hello\":\"websocket works!\"}"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        System.out.println("WS 断开，会话数=" + sessions.size());
    }

    // 允许前端发消息测试
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if ("ping".equalsIgnoreCase(message.getPayload())) {
            session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
        }
    }

    // MQTT->WS 广播：对象，乐队状态
    public void broadcast(InstrumentStatus status) {
        try {
            String json = mapper.writeValueAsString(status);
            broadcastRaw(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // MQTT->WS 广播：原始字符串（备用）
    public void broadcastRaw(String json) {
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                try { s.sendMessage(new TextMessage(json)); }
                catch (Exception e) { e.printStackTrace(); }
            }
        }
    }
}


