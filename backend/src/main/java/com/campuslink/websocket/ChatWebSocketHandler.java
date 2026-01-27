package com.campuslink.websocket;

import com.campuslink.dto.message.SendMessageRequest;
import com.campuslink.service.MessageService;
import com.campuslink.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 聊天处理器
 * 端点：/ws/chat/{token}
 */
@Slf4j
@Component
@ServerEndpoint("/ws/chat/{token}")
public class ChatWebSocketHandler {

    // 静态变量：存储所有在线用户的 WebSocket 会话
    // Key: userId, Value: Session
    private static final Map<Long, Session> onlineUsers = new ConcurrentHashMap<>();

    // 注入 Service（需要通过静态变量）
    private static MessageService messageService;
    private static JwtUtil jwtUtil;
    private static ObjectMapper objectMapper;

    @Autowired
    public void setMessageService(MessageService messageService) {
        ChatWebSocketHandler.messageService = messageService;
    }

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        ChatWebSocketHandler.jwtUtil = jwtUtil;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        ChatWebSocketHandler.objectMapper = objectMapper;
    }

    // 当前连接的用户ID
    private Long userId;

    /**
     * 连接建立时调用
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        try {
            // 验证 Token 并获取用户ID
            this.userId = jwtUtil.getUserIdFromToken(token);

            if (userId == null) {
                log.warn("WebSocket 连接失败：Token 无效");
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Invalid token"));
                return;
            }

            // 保存连接
            onlineUsers.put(userId, session);
            log.info("WebSocket 连接成功：userId={}, 当前在线用户数={}", userId, onlineUsers.size());

            // 发送连接成功消息
            sendMessage(session, Map.of(
                "type", "system",
                "message", "连接成功",
                "onlineCount", onlineUsers.size()
            ));

        } catch (Exception e) {
            log.error("WebSocket 连接异常", e);
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Connection error"));
            } catch (IOException ex) {
                log.error("关闭 WebSocket 连接失败", ex);
            }
        }
    }

    /**
     * 收到客户端消息时调用
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            log.info("收到消息：userId={}, message={}", userId, message);

            // 解析消息
            @SuppressWarnings("unchecked")
            Map<String, Object> msgData = objectMapper.readValue(message, Map.class);
            String type = (String) msgData.get("type");

            if ("chat".equals(type)) {
                // 聊天消息
                handleChatMessage(msgData);
            } else if ("ping".equals(type)) {
                // 心跳消息
                sendMessage(session, Map.of("type", "pong"));
            } else if ("typing".equals(type)) {
                // 输入状态提示
                handleTypingMessage(msgData);
            } else if ("read".equals(type)) {
                // 消息已读
                handleReadMessage(msgData);
            } else if ("recall".equals(type)) {
                // 消息撤回
                handleRecallMessage(msgData);
            }

        } catch (Exception e) {
            log.error("处理消息异常", e);
            sendMessage(session, Map.of(
                "type", "error",
                "message", "消息处理失败"
            ));
        }
    }

    /**
     * 连接关闭时调用
     */
    @OnClose
    public void onClose() {
        if (userId != null) {
            onlineUsers.remove(userId);
            log.info("WebSocket 连接关闭：userId={}, 当前在线用户数={}", userId, onlineUsers.size());
        }
    }

    /**
     * 连接错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 错误：userId={}", userId, error);
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMessage(Map<String, Object> msgData) {
        try {
            Long toUserId = Long.valueOf(msgData.get("toUserId").toString());
            String content = (String) msgData.get("content");
            Integer msgType = msgData.get("msgType") != null
                ? Integer.valueOf(msgData.get("msgType").toString())
                : 1; // 默认文本消息

            // 保存消息到数据库
            SendMessageRequest request = new SendMessageRequest();
            request.setReceiverId(toUserId);
            request.setContent(content);
            request.setMsgType(msgType);

            Long messageId = messageService.sendMessage(userId, request);
            log.info("消息已保存到数据库：messageId={}", messageId);

            // 推送消息给接收方（如果在线）
            Session receiverSession = onlineUsers.get(toUserId);
            if (receiverSession != null && receiverSession.isOpen()) {
                sendMessage(receiverSession, Map.of(
                    "type", "chat",
                    "messageId", messageId,
                    "fromUserId", userId,
                    "toUserId", toUserId,
                    "content", content,
                    "msgType", msgType,
                    "timestamp", System.currentTimeMillis()
                ));
                log.info("消息已推送给接收方：toUserId={}", toUserId);
            } else {
                log.info("接收方不在线，消息已保存：toUserId={}", toUserId);
            }

            // 回复发送方（发送成功）
            Session senderSession = onlineUsers.get(userId);
            if (senderSession != null && senderSession.isOpen()) {
                sendMessage(senderSession, Map.of(
                    "type", "send_success",
                    "messageId", messageId,
                    "toUserId", toUserId,
                    "content", content,
                    "msgType", msgType,
                    "timestamp", System.currentTimeMillis()
                ));
            }
        } catch (Exception e) {
            log.error("处理聊天消息失败：userId={}", userId, e);
            Session senderSession = onlineUsers.get(userId);
            if (senderSession != null && senderSession.isOpen()) {
                sendMessage(senderSession, Map.of(
                    "type", "send_error",
                    "message", "发送失败：" + e.getMessage()
                ));
            }
        }
    }

    /**
     * 处理输入状态消息
     */
    private void handleTypingMessage(Map<String, Object> msgData) {
        try {
            Long toUserId = Long.valueOf(msgData.get("toUserId").toString());
            Boolean isTyping = msgData.get("isTyping") != null
                ? Boolean.valueOf(msgData.get("isTyping").toString())
                : false;

            log.info("输入状态变更：fromUserId={}, toUserId={}, isTyping={}", userId, toUserId, isTyping);

            // 推送给接收方（如果在线）
            Session receiverSession = onlineUsers.get(toUserId);
            if (receiverSession != null && receiverSession.isOpen()) {
                sendMessage(receiverSession, Map.of(
                    "type", "typing",
                    "fromUserId", userId,
                    "toUserId", toUserId,
                    "isTyping", isTyping,
                    "timestamp", System.currentTimeMillis()
                ));
                log.info("输入状态已推送给接收方：toUserId={}", toUserId);
            } else {
                log.info("接收方不在线，忽略输入状态：toUserId={}", toUserId);
            }

        } catch (Exception e) {
            log.error("处理输入状态消息失败：userId={}", userId, e);
        }
    }

    /**
     * 处理消息已读
     */
    private void handleReadMessage(Map<String, Object> msgData) {
        try {
            Long fromUserId = Long.valueOf(msgData.get("fromUserId").toString());

            log.info("标记消息已读：readerId={}, fromUserId={}", userId, fromUserId);

            // 调用 Service 标记消息为已读
            messageService.markAsRead(userId, fromUserId);

            // 通知发送方（如果在线）
            Session senderSession = onlineUsers.get(fromUserId);
            if (senderSession != null && senderSession.isOpen()) {
                sendMessage(senderSession, Map.of(
                    "type", "read_receipt",
                    "readerId", userId,
                    "timestamp", System.currentTimeMillis()
                ));
                log.info("已读回执已推送给发送方：fromUserId={}", fromUserId);
            }

        } catch (Exception e) {
            log.error("处理消息已读失败：userId={}", userId, e);
        }
    }

    /**
     * 处理消息撤回
     */
    private void handleRecallMessage(Map<String, Object> msgData) {
        try {
            Long messageId = Long.valueOf(msgData.get("messageId").toString());

            log.info("尝试撤回消息：userId={}, messageId={}", userId, messageId);

            // 调用 Service 撤回消息（会验证权限和时间限制）
            Long receiverId = messageService.recallMessage(userId, messageId);

            // 通知接收方（如果在线）
            if (receiverId != null) {
                Session receiverSession = onlineUsers.get(receiverId);
                if (receiverSession != null && receiverSession.isOpen()) {
                    sendMessage(receiverSession, Map.of(
                        "type", "message_recalled",
                        "messageId", messageId,
                        "fromUserId", userId,
                        "timestamp", System.currentTimeMillis()
                    ));
                    log.info("撤回通知已推送给接收方：receiverId={}", receiverId);
                }
            }

            // 通知发送方撤回成功
            Session senderSession = onlineUsers.get(userId);
            if (senderSession != null && senderSession.isOpen()) {
                sendMessage(senderSession, Map.of(
                    "type", "recall_success",
                    "messageId", messageId,
                    "timestamp", System.currentTimeMillis()
                ));
            }

        } catch (Exception e) {
            log.error("处理消息撤回失败：userId={}", userId, e);
            // 通知发送方撤回失败
            Session senderSession = onlineUsers.get(userId);
            if (senderSession != null && senderSession.isOpen()) {
                sendMessage(senderSession, Map.of(
                    "type", "recall_error",
                    "message", e.getMessage()
                ));
            }
        }
    }

    /**
     * 发送消息给指定 Session
     */
    private void sendMessage(Session session, Object message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.getBasicRemote().sendText(json);
        } catch (Exception e) {
            log.error("发送消息失败", e);
        }
    }

    /**
     * 向指定用户推送消息（静态方法，供外部调用）
     */
    public static void sendToUser(Long userId, Object message) {
        Session session = onlineUsers.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(message);
                session.getBasicRemote().sendText(json);
            } catch (Exception e) {
                log.error("推送消息失败：userId={}", userId, e);
            }
        }
    }

    /**
     * 获取在线用户数
     */
    public static int getOnlineCount() {
        return onlineUsers.size();
    }

    /**
     * 判断用户是否在线
     */
    public static boolean isUserOnline(Long userId) {
        return onlineUsers.containsKey(userId);
    }
}
