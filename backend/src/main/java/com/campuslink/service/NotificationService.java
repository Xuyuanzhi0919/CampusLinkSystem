package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.notification.NotificationResponse;
import com.campuslink.dto.notification.SendNotificationRequest;
import com.campuslink.entity.Notification;
import com.campuslink.entity.User;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.NotificationMapper;
import com.campuslink.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通知服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    // 通知类型映射
    private static final Map<String, String> NOTIFY_TYPE_MAP = new HashMap<>();

    static {
        NOTIFY_TYPE_MAP.put("system", "系统通知");
        NOTIFY_TYPE_MAP.put("answer", "回答通知");
        NOTIFY_TYPE_MAP.put("task", "任务通知");
        NOTIFY_TYPE_MAP.put("activity", "活动通知");
        NOTIFY_TYPE_MAP.put("message", "私信通知");
        NOTIFY_TYPE_MAP.put("report", "举报通知");
        NOTIFY_TYPE_MAP.put("club", "社团通知");
    }

    /**
     * 发送通知(管理员)
     */
    @Transactional
    public void sendNotification(SendNotificationRequest request) {
        if (request.getUserId() != null) {
            // 发送给指定用户
            sendToUser(request.getUserId(), request);
        } else {
            // 发送给所有用户
            sendToAllUsers(request);
        }
    }

    /**
     * 发送给指定用户
     */
    @Transactional
    public void sendToUser(Long userId, SendNotificationRequest request) {
        // 验证用户存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        createNotification(userId, request);
        log.info("已向用户 {} 发送通知: {}", userId, request.getTitle());
    }

    /**
     * 发送给所有用户
     */
    @Async
    @Transactional
    public void sendToAllUsers(SendNotificationRequest request) {
        // 查询所有正常状态的用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, 1);
        List<User> users = userMapper.selectList(wrapper);

        for (User user : users) {
            createNotification(user.getUId(), request);
        }

        log.info("已向所有用户({})发送通知: {}", users.size(), request.getTitle());
    }

    /**
     * 创建通知记录
     */
    private void createNotification(Long userId, SendNotificationRequest request) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setNotifyType(request.getNotifyType());
        notification.setRelatedType(request.getRelatedType());
        notification.setRelatedId(request.getRelatedId());
        notification.setIsRead(0);
        notification.setCreatedAt(LocalDateTime.now());

        notificationMapper.insert(notification);
    }

    /**
     * 获取我的通知列表
     */
    public PageResult<NotificationResponse> getMyNotifications(Long userId, Integer page, Integer pageSize, String type) {
        Page<Notification> notificationPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);

        // 如果指定了类型，则过滤
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notification::getNotifyType, type);
        }

        wrapper.orderByDesc(Notification::getCreatedAt);

        notificationPage = notificationMapper.selectPage(notificationPage, wrapper);

        List<NotificationResponse> responses = notificationPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageResult<>(
                responses,
                notificationPage.getTotal(),
                notificationPage.getCurrent(),
                notificationPage.getSize(),
                notificationPage.getPages()
        );
    }

    /**
     * 获取未读通知列表
     */
    public PageResult<NotificationResponse> getUnreadNotifications(Long userId, Integer page, Integer pageSize) {
        Page<Notification> notificationPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .orderByDesc(Notification::getCreatedAt);

        notificationPage = notificationMapper.selectPage(notificationPage, wrapper);

        List<NotificationResponse> responses = notificationPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageResult<>(
                responses,
                notificationPage.getTotal(),
                notificationPage.getCurrent(),
                notificationPage.getSize(),
                notificationPage.getPages()
        );
    }

    /**
     * 获取未读通知数量
     */
    public Integer getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0);

        return Math.toIntExact(notificationMapper.selectCount(wrapper));
    }

    /**
     * 标记通知为已读
     */
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
        }

        // 验证权限
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        if (notification.getIsRead() == 0) {
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
            log.info("用户 {} 标记通知 {} 为已读", userId, notificationId);
        }
    }

    /**
     * 标记所有通知为已读
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0);

        List<Notification> notifications = notificationMapper.selectList(wrapper);

        for (Notification notification : notifications) {
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
        }

        log.info("用户 {} 标记了 {} 条通知为已读", userId, notifications.size());
    }

    /**
     * 删除通知
     */
    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
        }

        // 验证权限
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        notificationMapper.deleteById(notificationId);
        log.info("用户 {} 删除了通知 {}", userId, notificationId);
    }

    /**
     * 清空所有已读通知
     */
    @Transactional
    public void clearReadNotifications(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 1);

        int count = Math.toIntExact(notificationMapper.selectCount(wrapper));
        notificationMapper.delete(wrapper);

        log.info("用户 {} 清空了 {} 条已读通知", userId, count);
    }

    /**
     * 转换为响应对象
     */
    private NotificationResponse convertToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        BeanUtils.copyProperties(notification, response);
        response.setIsRead(notification.getIsRead() == 1);
        response.setNotifyTypeName(NOTIFY_TYPE_MAP.getOrDefault(notification.getNotifyType(), "未知类型"));
        return response;
    }
}
