package com.campuslink.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.dto.notification.SendNotificationRequest;
import com.campuslink.entity.Notification;
import com.campuslink.event.MessageSentEvent;
import com.campuslink.mapper.NotificationMapper;
import com.campuslink.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * 私信通知事件监听器
 *
 * 核心设计:
 * 1. @TransactionalEventListener(phase = AFTER_COMMIT): 确保只在事务提交后触发
 * 2. @Async: 异步处理,不阻塞主业务流程
 * 3. 智能内容生成: 根据消息类型生成不同的通知文案
 * 4. 通知聚合: 5分钟内来自同一用户的通知聚合为一条
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageNotificationListener {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    /**
     * 通知聚合时间窗口 (分钟)
     */
    private static final int AGGREGATION_WINDOW_MINUTES = 5;

    /**
     * 监听私信发送事件,创建或聚合通知
     *
     * 关键点:
     * - phase = AFTER_COMMIT: 只在 MessageService 事务提交成功后执行
     * - @Async("notificationExecutor"): 使用专用线程池异步执行
     * - REQUIRES_NEW 事务: 在新事务中执行,确保查询和更新的原子性,行锁防止并发问题
     * - 通知聚合: 5分钟内的通知聚合为一条,避免刷屏
     */
    @Async("notificationExecutor")
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageSentEvent(MessageSentEvent event) {
        try {
            log.debug("处理私信通知事件: messageId={}, sender={}, receiver={}",
                event.getMessageId(), event.getSenderId(), event.getReceiverId());

            String displayName = event.getSenderNickname() != null && !event.getSenderNickname().isEmpty()
                ? event.getSenderNickname()
                : "用户" + event.getSenderId();

            // 1. 查询是否存在5分钟内来自同一发送者的未读通知
            LocalDateTime aggregationThreshold = LocalDateTime.now().minusMinutes(AGGREGATION_WINDOW_MINUTES);

            LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Notification::getUserId, event.getReceiverId())
                   .eq(Notification::getNotifyType, "message")
                   .eq(Notification::getRelatedId, event.getSenderId())
                   .eq(Notification::getIsRead, 0)
                   .ge(Notification::getCreatedAt, aggregationThreshold)
                   .orderByDesc(Notification::getCreatedAt)
                   .last("LIMIT 1 FOR UPDATE");  // 行锁,防止并发更新

            Notification existingNotification = notificationMapper.selectOne(wrapper);

            if (existingNotification != null) {
                // 2. 存在未读通知,更新为聚合通知
                existingNotification.setContent(String.format("%s 给您发送了多条私信", displayName));
                existingNotification.setCreatedAt(LocalDateTime.now());
                notificationMapper.updateById(existingNotification);

                log.info("聚合私信通知: notificationId={}, sender={}, receiver={}",
                    existingNotification.getNotificationId(), event.getSenderId(), event.getReceiverId());

            } else {
                // 3. 不存在未读通知,创建新通知
                SendNotificationRequest request = new SendNotificationRequest();
                request.setUserId(event.getReceiverId());
                request.setTitle("您收到了新私信");
                request.setContent(buildNotificationContent(
                    event.getSenderNickname(),
                    event.getSenderId(),
                    event.getMsgType(),
                    event.getContent()
                ));
                request.setNotifyType("message");
                request.setRelatedType("MESSAGE");
                request.setRelatedId(event.getSenderId());

                notificationService.sendToUser(event.getReceiverId(), request);

                log.info("创建私信通知: messageId={}, sender={}, receiver={}",
                    event.getMessageId(), event.getSenderId(), event.getReceiverId());
            }

        } catch (Exception e) {
            // 异常不影响消息发送,但需要记录日志
            log.error("私信通知处理失败: messageId={}, error={}",
                event.getMessageId(), e.getMessage(), e);

            // TODO: 可以将失败事件推送到死信队列,后续补偿处理
        }
    }

    /**
     * 根据消息类型生成智能通知内容
     *
     * 设计要点:
     * 1. 文本消息(1): 显示前 20 个字符预览
     * 2. 图片消息(2): 显示类型化提示
     * 3. 文件消息(3): 显示类型化提示
     * 4. 敏感词过滤: TODO 后续集成敏感词检测
     */
    private String buildNotificationContent(String senderNickname, Long senderId,
                                            Integer msgType, String content) {
        String displayName = senderNickname != null && !senderNickname.isEmpty()
            ? senderNickname
            : "用户" + senderId;

        if (msgType == null) {
            return String.format("%s 给您发送了私信", displayName);
        }

        switch (msgType) {
            case 1:  // 文本消息
                // 文本消息: 显示内容预览
                if (content != null && !content.isEmpty()) {
                    // 安全截断,避免截断 emoji
                    String preview = truncateSafely(content, 20);
                    return String.format("%s: %s", displayName, preview);
                }
                return String.format("%s 给您发送了私信", displayName);

            case 2:  // 图片消息
                return String.format("%s 给您发送了一张图片", displayName);

            case 3:  // 文件消息
                return String.format("%s 给您发送了一个文件", displayName);

            default:
                return String.format("%s 给您发送了私信", displayName);
        }
    }

    /**
     * 安全截断字符串,避免截断 emoji 或 surrogate pair
     *
     * @param text 原始文本
     * @param maxLength 最大长度(字符数,非字节数)
     * @return 截断后的文本
     */
    private String truncateSafely(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }

        // 使用 codePointCount 正确处理 emoji 和特殊字符
        int actualLength = text.codePointCount(0, text.length());
        if (actualLength <= maxLength) {
            return text;
        }

        // 找到第 maxLength 个 code point 的偏移量
        int offset = text.offsetByCodePoints(0, maxLength);
        return text.substring(0, offset) + "...";
    }
}
