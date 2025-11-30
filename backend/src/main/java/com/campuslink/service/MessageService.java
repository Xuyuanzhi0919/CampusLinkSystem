package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.message.ConversationResponse;
import com.campuslink.dto.message.MessageResponse;
import com.campuslink.dto.message.SendMessageRequest;
import com.campuslink.entity.Message;
import com.campuslink.entity.User;
import com.campuslink.event.MessageSentEvent;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.MessageMapper;
import com.campuslink.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 私信服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 消息发送限流: 每分钟最多发送条数
     */
    private static final int MAX_MESSAGES_PER_MINUTE = 20;

    /**
     * 群发检测阈值: 1分钟内给不同用户发送消息的数量
     */
    private static final int MASS_SEND_THRESHOLD = 30;

    /**
     * 发送私信
     *
     * 优化点:
     * 1. 使用事件驱动模式,通知创建在事务提交后异步执行
     * 2. 提升性能: 私信发送不再被通知创建阻塞
     * 3. 解耦: MessageService 不再依赖 NotificationService
     * 4. 限流防护: 防止消息轰炸和群发滥用
     */
    @Transactional
    public Long sendMessage(Long senderId, SendMessageRequest request) {
        // 1. 消息发送限流: 每分钟最多发送 20 条
        String msgRateKey = String.format("msg:rate:%d:%d", senderId, request.getReceiverId());
        Long msgCount = redisTemplate.opsForValue().increment(msgRateKey);

        if (msgCount == 1) {
            redisTemplate.expire(msgRateKey, 1, TimeUnit.MINUTES);
        }

        if (msgCount > MAX_MESSAGES_PER_MINUTE) {
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS,
                "发送消息过于频繁,请稍后再试");
        }

        // 2. 群发检测: 1分钟内给超过 30 个不同用户发消息
        String massSendKey = String.format("msg:receivers:%d", senderId);
        redisTemplate.opsForSet().add(massSendKey, request.getReceiverId().toString());
        redisTemplate.expire(massSendKey, 1, TimeUnit.MINUTES);

        Long uniqueReceiverCount = redisTemplate.opsForSet().size(massSendKey);
        if (uniqueReceiverCount != null && uniqueReceiverCount > MASS_SEND_THRESHOLD) {
            log.warn("检测到疑似群发行为: senderId={}, uniqueReceivers={}",
                senderId, uniqueReceiverCount);
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS,
                "您的消息发送过于频繁,已被系统限制");
        }

        // 3. 验证接收者存在
        User receiver = userMapper.selectById(request.getReceiverId());
        if (receiver == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 4. 不能给自己发消息
        if (senderId.equals(request.getReceiverId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能给自己发送消息");
        }

        // 获取发送者信息(用于事件)
        User sender = userMapper.selectById(senderId);

        // 创建消息
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(request.getReceiverId());
        message.setContent(request.getContent());
        message.setMsgType(request.getMsgType());
        message.setIsRead(0);
        message.setCreatedAt(LocalDateTime.now());

        messageMapper.insert(message);

        // 发布私信发送事件 (事务提交后才会触发监听器)
        MessageSentEvent event = new MessageSentEvent(
            this,
            message.getMId(),
            senderId,
            sender != null ? sender.getNickname() : null,
            request.getReceiverId(),
            request.getMsgType(),
            request.getContent()
        );
        eventPublisher.publishEvent(event);

        log.info("用户 {} 向用户 {} 发送了消息", senderId, request.getReceiverId());
        return message.getMId();
    }

    /**
     * 获取与某个用户的聊天记录
     */
    public PageResult<MessageResponse> getChatHistory(Long userId, Long otherUserId, Integer page, Integer pageSize) {
        Page<Message> messagePage = new Page<>(page, pageSize);

        // 查询两个用户之间的消息
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .and(w1 -> w1.eq(Message::getSenderId, userId).eq(Message::getReceiverId, otherUserId))
                .or(w2 -> w2.eq(Message::getSenderId, otherUserId).eq(Message::getReceiverId, userId))
        );
        wrapper.orderByDesc(Message::getCreatedAt);

        messagePage = messageMapper.selectPage(messagePage, wrapper);

        List<MessageResponse> messageResponses = messagePage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageResult<>(
                messageResponses,
                messagePage.getTotal(),
                messagePage.getCurrent(),
                messagePage.getSize(),
                messagePage.getPages()
        );
    }

    /**
     * 获取会话列表
     */
    public List<ConversationResponse> getConversationList(Long userId) {
        // 查询所有与该用户相关的消息
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Message::getSenderId, userId).or().eq(Message::getReceiverId, userId));
        wrapper.orderByDesc(Message::getCreatedAt);

        List<Message> messages = messageMapper.selectList(wrapper);

        // 按对方用户ID分组
        Map<Long, List<Message>> conversationMap = new HashMap<>();
        for (Message message : messages) {
            Long otherUserId = message.getSenderId().equals(userId) ?
                    message.getReceiverId() : message.getSenderId();

            conversationMap.computeIfAbsent(otherUserId, k -> new ArrayList<>()).add(message);
        }

        // 构建会话列表
        List<ConversationResponse> conversations = new ArrayList<>();
        for (Map.Entry<Long, List<Message>> entry : conversationMap.entrySet()) {
            Long otherUserId = entry.getKey();
            List<Message> conversationMessages = entry.getValue();

            ConversationResponse conversation = new ConversationResponse();
            conversation.setUserId(otherUserId);

            // 查询对方用户信息
            User otherUser = userMapper.selectById(otherUserId);
            if (otherUser != null) {
                conversation.setNickname(otherUser.getNickname());
                conversation.setAvatarUrl(otherUser.getAvatarUrl());
            }

            // 获取最后一条消息
            Message lastMessage = conversationMessages.get(0);
            conversation.setLastMessage(lastMessage.getContent());
            conversation.setLastMessageType(lastMessage.getMsgType());
            conversation.setLastMessageTime(lastMessage.getCreatedAt());

            // 计算未读消息数
            long unreadCount = conversationMessages.stream()
                    .filter(m -> m.getReceiverId().equals(userId) && m.getIsRead() == 0)
                    .count();
            conversation.setUnreadCount((int) unreadCount);

            conversations.add(conversation);
        }

        // 按最后消息时间排序
        conversations.sort((c1, c2) -> c2.getLastMessageTime().compareTo(c1.getLastMessageTime()));

        return conversations;
    }

    /**
     * 标记消息为已读
     */
    @Transactional
    public void markAsRead(Long userId, Long otherUserId) {
        // 查询所有来自对方且未读的消息
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getSenderId, otherUserId)
                .eq(Message::getReceiverId, userId)
                .eq(Message::getIsRead, 0);

        List<Message> unreadMessages = messageMapper.selectList(wrapper);

        for (Message message : unreadMessages) {
            message.setIsRead(1);
            messageMapper.updateById(message);
        }

        if (!unreadMessages.isEmpty()) {
            log.info("用户 {} 标记了 {} 条来自用户 {} 的消息为已读", userId, unreadMessages.size(), otherUserId);
        }
    }

    /**
     * 获取未读消息总数
     */
    public Integer getUnreadCount(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId)
                .eq(Message::getIsRead, 0);

        return Math.toIntExact(messageMapper.selectCount(wrapper));
    }

    /**
     * 撤回消息（2分钟内）
     */
    @Transactional
    public Long recallMessage(Long userId, Long messageId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "消息不存在");
        }

        // 只能撤回自己发送的消息
        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED, "只能撤回自己发送的消息");
        }

        // 检查是否在2分钟内
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = message.getCreatedAt();
        long minutesDiff = java.time.Duration.between(createdAt, now).toMinutes();

        if (minutesDiff > 2) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "消息发送超过2分钟，无法撤回");
        }

        // 删除消息
        Long receiverId = message.getReceiverId();
        messageMapper.deleteById(messageId);
        log.info("用户 {} 撤回了消息 {}", userId, messageId);

        return receiverId;
    }

    /**
     * 删除消息
     */
    @Transactional
    public void deleteMessage(Long userId, Long messageId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "消息不存在");
        }

        // 只能删除自己发送或接收的消息
        if (!message.getSenderId().equals(userId) && !message.getReceiverId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        messageMapper.deleteById(messageId);
        log.info("用户 {} 删除了消息 {}", userId, messageId);
    }

    /**
     * 转换为消息响应对象
     */
    private MessageResponse convertToResponse(Message message) {
        MessageResponse response = new MessageResponse();
        BeanUtils.copyProperties(message, response);
        response.setIsRead(message.getIsRead() == 1);

        // 查询发送者信息
        User sender = userMapper.selectById(message.getSenderId());
        if (sender != null) {
            response.setSenderName(sender.getNickname());
            response.setSenderAvatar(sender.getAvatarUrl());
        }

        // 查询接收者信息
        User receiver = userMapper.selectById(message.getReceiverId());
        if (receiver != null) {
            response.setReceiverName(receiver.getNickname());
            response.setReceiverAvatar(receiver.getAvatarUrl());
        }

        return response;
    }
}
