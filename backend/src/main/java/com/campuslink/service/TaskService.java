package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.notification.SendNotificationRequest;
import com.campuslink.dto.report.CreateReportRequest;
import com.campuslink.dto.task.*;
import com.campuslink.entity.PointsLog;
import com.campuslink.entity.Task;
import com.campuslink.entity.TaskLog;
import com.campuslink.entity.TaskRating;
import com.campuslink.entity.User;
import com.campuslink.enums.TaskStatus;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.PointsLogMapper;
import com.campuslink.mapper.TaskLogMapper;
import com.campuslink.mapper.TaskMapper;
import com.campuslink.mapper.TaskRatingMapper;
import com.campuslink.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务服务
 */
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final PointsLogMapper pointsLogMapper;
    private final FavoriteService favoriteService;
    private final ReportService reportService;
    private final TaskLogMapper taskLogMapper;
    private final TaskRatingMapper taskRatingMapper;
    private final NotificationService notificationService;

    /**
     * 发布任务
     */
    @Transactional(rollbackFor = Exception.class)
    public Long publishTask(Long userId, PublishTaskRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 检查用户积分是否足够
        if (user.getPoints() < request.getRewardPoints()) {
            throw new BusinessException(ResultCode.INSUFFICIENT_POINTS);
        }

        // 扣除悬赏积分
        Integer oldPoints = user.getPoints();
        user.setPoints(oldPoints - request.getRewardPoints());
        userMapper.updateById(user);

        // 创建任务
        Task task = new Task();
        task.setPublisherId(userId);
        task.setTitle(request.getTitle());
        task.setContent(request.getContent());
        task.setTaskType(request.getTaskType());
        task.setRewardPoints(request.getRewardPoints());
        task.setLocation(request.getLocation());
        task.setDeadline(request.getDeadline());
        task.setStatus(0); // 待接单
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        taskMapper.insert(task);

        // 记录积分日志
        recordPointsLog(
                userId,
                -request.getRewardPoints(),
                user.getPoints(),
                "发布任务：" + request.getTitle(),
                "task",
                task.getTid()
        );

        return task.getTid();
    }

    /**
     * 获取任务列表
     */
    public PageResult<TaskListResponse> getTaskList(
            String taskType, Integer status,
            Integer page, Integer pageSize, String sortBy, String sortOrder
    ) {
        // 构建查询条件
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();

        if (taskType != null && !taskType.isEmpty()) {
            wrapper.eq(Task::getTaskType, taskType);
        }
        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }

        // 排序
        if ("created_at".equals(sortBy)) {
            wrapper.orderBy(true, "asc".equals(sortOrder), Task::getCreatedAt);
        } else if ("reward_points".equals(sortBy)) {
            wrapper.orderBy(true, "asc".equals(sortOrder), Task::getRewardPoints);
        } else if ("deadline".equals(sortBy)) {
            wrapper.orderBy(true, "asc".equals(sortOrder), Task::getDeadline);
        }

        // 分页查询
        Page<Task> pageObj = new Page<>(page, pageSize);
        Page<Task> result = taskMapper.selectPage(pageObj, wrapper);

        // 转换为响应对象
        List<TaskListResponse> responses = new ArrayList<>();
        for (Task task : result.getRecords()) {
            TaskListResponse response = new TaskListResponse();
            response.setTid(task.getTid());
            response.setTitle(task.getTitle());
            response.setTaskType(task.getTaskType());
            response.setRewardPoints(task.getRewardPoints());
            response.setLocation(task.getLocation());
            response.setDeadline(task.getDeadline());
            response.setStatus(task.getStatus());
            response.setCreatedAt(task.getCreatedAt());

            // 获取发布者昵称
            User publisher = userMapper.selectById(task.getPublisherId());
            if (publisher != null) {
                response.setPublisherNickname(publisher.getNickname());
            }

            responses.add(response);
        }

        // 构建分页结果
        PageResult<TaskListResponse> pageResult = new PageResult<>();
        pageResult.setList(responses);
        pageResult.setTotal(result.getTotal());
        pageResult.setPage((long) page);
        pageResult.setPageSize((long) pageSize);
        pageResult.setTotalPages((result.getTotal() + pageSize - 1) / pageSize);

        return pageResult;
    }

    /**
     * 获取任务详情
     */
    public TaskResponse getTaskById(Long taskId, Long userId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        // 转换为响应对象
        TaskResponse response = new TaskResponse();
        response.setTid(task.getTid());
        response.setPublisherId(task.getPublisherId());
        response.setTitle(task.getTitle());
        response.setContent(task.getContent());
        response.setTaskType(task.getTaskType());
        response.setRewardPoints(task.getRewardPoints());
        response.setLocation(task.getLocation());
        response.setDeadline(task.getDeadline());
        response.setAccepterId(task.getAccepterId());
        response.setStatus(task.getStatus());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        response.setCompletedAt(task.getCompletedAt());

        // 获取发布者信息
        User publisher = userMapper.selectById(task.getPublisherId());
        if (publisher != null) {
            response.setPublisherNickname(publisher.getNickname());
            response.setPublisherAvatar(publisher.getAvatarUrl());
        }

        // 获取接单者信息（如果有）
        if (task.getAccepterId() != null) {
            User accepter = userMapper.selectById(task.getAccepterId());
            if (accepter != null) {
                response.setAccepterNickname(accepter.getNickname());
                response.setAccepterAvatar(accepter.getAvatarUrl());
            }
        } else {
            // 显式设置为 null，确保放弃任务后接单者信息被清除
            response.setAccepterNickname(null);
            response.setAccepterAvatar(null);
        }

        // 查询当前用户是否收藏了该任务
        if (userId != null) {
            boolean isFavorited = favoriteService.isFavorited(userId, "task", taskId);
            response.setIsFavorited(isFavorited);
        } else {
            response.setIsFavorited(false);
        }

        return response;
    }

    /**
     * 接单
     */
    @Transactional(rollbackFor = Exception.class)
    public void acceptTask(Long userId, Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        // 检查任务状态
        if (task.getStatus() != 0) {
            throw new BusinessException(ResultCode.TASK_ALREADY_ACCEPTED);
        }

        // 不能接受自己发布的任务
        if (task.getPublisherId().equals(userId)) {
            throw new BusinessException(ResultCode.CANNOT_ACCEPT_OWN_TASK);
        }

        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 更新任务状态为进行中(接单后直接开始)
        task.setAccepterId(userId);
        task.setStatus(TaskStatus.IN_PROGRESS.getCode()); // 进行中
        task.setAcceptedAt(LocalDateTime.now()); // 记录接单时间
        task.setStartedAt(LocalDateTime.now()); // 同时记录开始时间
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        // 记录操作日志
        recordTaskLog(taskId, userId, "accept", TaskStatus.ACTIVE.getCode(),
            TaskStatus.IN_PROGRESS.getCode(), "用户接取任务并开始执行");

        // 发送通知给发布者
        SendNotificationRequest notificationRequest = new SendNotificationRequest();
        notificationRequest.setUserId(task.getPublisherId());
        notificationRequest.setTitle("您的任务有人接单了");
        notificationRequest.setContent(String.format("用户 %s 接受了您的任务「%s」", user.getNickname(), task.getTitle()));
        notificationRequest.setNotifyType("task");
        notificationRequest.setRelatedType("task");
        notificationRequest.setRelatedId(taskId);
        notificationService.sendToUser(task.getPublisherId(), notificationRequest);
    }

    /**
     * 完成任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(Long userId, Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        // 只有发布者可以标记任务为已完成
        if (!task.getPublisherId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        // 检查任务状态(必须是待确认状态)
        if (task.getStatus() != TaskStatus.SUBMITTED.getCode()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(),
                "当前任务状态不允许确认完成,任务状态: " + TaskStatus.fromCode(task.getStatus()).getName());
        }

        // 更新任务状态为已完成
        task.setStatus(TaskStatus.COMPLETED.getCode()); // 已完成 (status=4)
        task.setCompletedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        // 奖励接单者
        if (task.getAccepterId() != null) {
            User accepter = userMapper.selectById(task.getAccepterId());
            if (accepter != null) {
                Integer oldPoints = accepter.getPoints();
                accepter.setPoints(oldPoints + task.getRewardPoints());
                userMapper.updateById(accepter);

                // 记录积分日志
                recordPointsLog(
                        accepter.getUId(),
                        task.getRewardPoints(),
                        accepter.getPoints(),
                        "完成任务：" + task.getTitle(),
                        "task",
                        task.getTid()
                );

                // 通知接单者：任务已完成，获得积分奖励
                SendNotificationRequest accepterNotification = new SendNotificationRequest();
                accepterNotification.setUserId(accepter.getUId());
                accepterNotification.setTitle("任务已完成");
                accepterNotification.setContent(String.format("您完成的任务「%s」已被确认，获得 %d 积分奖励！",
                    task.getTitle(), task.getRewardPoints()));
                accepterNotification.setNotifyType("task");
                accepterNotification.setRelatedType("task");
                accepterNotification.setRelatedId(taskId);
                notificationService.sendToUser(accepter.getUId(), accepterNotification);
            }
        }

        // 通知发布者：任务已确认完成
        User publisher = userMapper.selectById(task.getPublisherId());
        if (publisher != null) {
            SendNotificationRequest publisherNotification = new SendNotificationRequest();
            publisherNotification.setUserId(publisher.getUId());
            publisherNotification.setTitle("任务已确认完成");
            publisherNotification.setContent(String.format("您发布的任务「%s」已确认完成", task.getTitle()));
            publisherNotification.setNotifyType("task");
            publisherNotification.setRelatedType("task");
            publisherNotification.setRelatedId(taskId);
            notificationService.sendToUser(publisher.getUId(), publisherNotification);
        }
    }

    /**
     * 取消任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelTask(Long userId, Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        // 只有发布者可以取消任务
        if (!task.getPublisherId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        // 只能取消待接单或进行中的任务
        if (task.getStatus() != 0 && task.getStatus() != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "只能取消待接单或进行中的任务");
        }

        // 更新任务状态
        task.setStatus(3); // 已取消
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        // 退还悬赏积分给发布者
        User publisher = userMapper.selectById(task.getPublisherId());
        if (publisher != null) {
            Integer oldPoints = publisher.getPoints();
            publisher.setPoints(oldPoints + task.getRewardPoints());
            userMapper.updateById(publisher);

            // 记录积分日志
            recordPointsLog(
                    publisher.getUId(),
                    task.getRewardPoints(),
                    publisher.getPoints(),
                    "取消任务退还积分：" + task.getTitle(),
                    "task",
                    task.getTid()
            );
        }
    }

    /**
     * 放弃任务(接单者)
     */
    @Transactional(rollbackFor = Exception.class)
    public void abandonTask(Long taskId, Long userId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        // 检查是否是接单者
        if (task.getAccepterId() == null || !task.getAccepterId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED.getCode(), "只有接单者可以放弃任务");
        }

        // 检查任务状态(只有进行中的任务可以放弃)
        TaskStatus currentStatus = TaskStatus.fromCode(task.getStatus());
        if (currentStatus != TaskStatus.IN_PROGRESS) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(),
                "当前任务状态不允许放弃,任务状态: " + currentStatus.getName());
        }

        // 更新任务状态:重新变回待接单状态
        Integer oldStatus = task.getStatus();
        task.setStatus(TaskStatus.ACTIVE.getCode()); // 重新变回待接单
        task.setAccepterId(null); // 清空接单者
        task.setAcceptedAt(null); // 清空接单时间
        task.setStartedAt(null); // 清空开始时间
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        // 记录操作日志
        recordTaskLog(
            taskId,
            userId,
            "abandon",
            oldStatus,
            TaskStatus.ACTIVE.getCode(),
            "接单者放弃任务"
        );

        // 通知发布者
        User publisher = userMapper.selectById(task.getPublisherId());
        if (publisher != null) {
            SendNotificationRequest notification = new SendNotificationRequest();
            notification.setUserId(publisher.getUId());
            notification.setTitle("任务已被放弃");
            notification.setContent(String.format("您发布的任务「%s」已被接单者放弃,任务重新回到待接单状态", task.getTitle()));
            notification.setNotifyType("task");
            notification.setRelatedType("task");
            notification.setRelatedId(taskId);
            notificationService.sendToUser(publisher.getUId(), notification);
        }
    }

    /**
     * 获取我发布的任务
     */
    public PageResult<TaskListResponse> getMyPublishedTasks(
            Long userId, Integer status,
            Integer page, Integer pageSize
    ) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getPublisherId, userId);

        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }

        wrapper.orderByDesc(Task::getCreatedAt);

        Page<Task> pageObj = new Page<>(page, pageSize);
        Page<Task> result = taskMapper.selectPage(pageObj, wrapper);

        List<TaskListResponse> responses = new ArrayList<>();
        for (Task task : result.getRecords()) {
            TaskListResponse response = new TaskListResponse();
            response.setTid(task.getTid());
            response.setTitle(task.getTitle());
            response.setTaskType(task.getTaskType());
            response.setRewardPoints(task.getRewardPoints());
            response.setLocation(task.getLocation());
            response.setDeadline(task.getDeadline());
            response.setStatus(task.getStatus());
            response.setCreatedAt(task.getCreatedAt());

            User publisher = userMapper.selectById(task.getPublisherId());
            if (publisher != null) {
                response.setPublisherNickname(publisher.getNickname());
            }

            responses.add(response);
        }

        PageResult<TaskListResponse> pageResult = new PageResult<>();
        pageResult.setList(responses);
        pageResult.setTotal(result.getTotal());
        pageResult.setPage((long) page);
        pageResult.setPageSize((long) pageSize);
        pageResult.setTotalPages((result.getTotal() + pageSize - 1) / pageSize);

        return pageResult;
    }

    /**
     * 获取我接受的任务
     */
    public PageResult<TaskListResponse> getMyAcceptedTasks(
            Long userId, Integer status,
            Integer page, Integer pageSize
    ) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getAccepterId, userId);

        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }

        wrapper.orderByDesc(Task::getCreatedAt);

        Page<Task> pageObj = new Page<>(page, pageSize);
        Page<Task> result = taskMapper.selectPage(pageObj, wrapper);

        List<TaskListResponse> responses = new ArrayList<>();
        for (Task task : result.getRecords()) {
            TaskListResponse response = new TaskListResponse();
            response.setTid(task.getTid());
            response.setTitle(task.getTitle());
            response.setTaskType(task.getTaskType());
            response.setRewardPoints(task.getRewardPoints());
            response.setLocation(task.getLocation());
            response.setDeadline(task.getDeadline());
            response.setStatus(task.getStatus());
            response.setCreatedAt(task.getCreatedAt());

            User publisher = userMapper.selectById(task.getPublisherId());
            if (publisher != null) {
                response.setPublisherNickname(publisher.getNickname());
            }

            responses.add(response);
        }

        PageResult<TaskListResponse> pageResult = new PageResult<>();
        pageResult.setList(responses);
        pageResult.setTotal(result.getTotal());
        pageResult.setPage((long) page);
        pageResult.setPageSize((long) pageSize);
        pageResult.setTotalPages((result.getTotal() + pageSize - 1) / pageSize);

        return pageResult;
    }

    /**
     * 记录积分变化日志
     */
    private void recordPointsLog(Long userId, Integer pointsChange, Integer pointsAfter,
                                   String reason, String relatedType, Long relatedId) {
        PointsLog pointsLog = new PointsLog();
        pointsLog.setUserId(userId);
        pointsLog.setPointsChange(pointsChange);
        pointsLog.setPointsAfter(pointsAfter);
        pointsLog.setReason(reason);
        pointsLog.setRelatedType(relatedType);
        pointsLog.setRelatedId(relatedId);
        pointsLog.setCreatedAt(LocalDateTime.now());
        pointsLogMapper.insert(pointsLog);
    }

    /**
     * 删除任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long taskId, Long userId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        // 只有发布者可以删除任务
        if (!task.getPublisherId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        // 只能删除待接单或已取消的任务
        if (task.getStatus() != 0 && task.getStatus() != 3) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "只能删除待接单或已取消的任务");
        }

        // 如果是待接单状态,退还积分
        if (task.getStatus() == 0) {
            User publisher = userMapper.selectById(task.getPublisherId());
            if (publisher != null) {
                Integer oldPoints = publisher.getPoints();
                publisher.setPoints(oldPoints + task.getRewardPoints());
                userMapper.updateById(publisher);

                // 记录积分日志
                recordPointsLog(
                        publisher.getUId(),
                        task.getRewardPoints(),
                        publisher.getPoints(),
                        "删除任务退还积分：" + task.getTitle(),
                        "task",
                        task.getTid()
                );
            }
        }

        // 删除任务
        taskMapper.deleteById(taskId);
    }

    /**
     * 获取相似任务推荐
     */
    public List<TaskListResponse> getSimilarTasks(Long taskId, Integer limit) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        // 查询相同类型的任务,排除当前任务
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getTaskType, task.getTaskType())
                .ne(Task::getTid, taskId)
                .eq(Task::getStatus, 0) // 只推荐待接单的任务
                .orderByDesc(Task::getCreatedAt)
                .last("LIMIT " + limit);

        List<Task> tasks = taskMapper.selectList(wrapper);

        // 转换为响应对象
        return tasks.stream().map(t -> {
            TaskListResponse response = new TaskListResponse();
            response.setTid(t.getTid());
            response.setTitle(t.getTitle());
            response.setTaskType(t.getTaskType());
            response.setRewardPoints(t.getRewardPoints());
            response.setLocation(t.getLocation());
            response.setDeadline(t.getDeadline());
            response.setStatus(t.getStatus());
            response.setCreatedAt(t.getCreatedAt());

            // 获取发布者昵称
            User publisher = userMapper.selectById(t.getPublisherId());
            if (publisher != null) {
                response.setPublisherNickname(publisher.getNickname());
            }

            return response;
        }).collect(Collectors.toList());
    }

    /**
     * 获取发布者联系方式
     */
    public Map<String, Object> getPublisherContact(Long taskId, Long userId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        User publisher = userMapper.selectById(task.getPublisherId());
        if (publisher == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 返回发布者信息
        Map<String, Object> result = new HashMap<>();
        result.put("chatId", "task_" + taskId + "_" + userId); // 私信会话ID

        Map<String, Object> publisherInfo = new HashMap<>();
        publisherInfo.put("userId", publisher.getUId());
        publisherInfo.put("nickname", publisher.getNickname());
        publisherInfo.put("avatar", publisher.getAvatarUrl());

        result.put("publisherInfo", publisherInfo);

        return result;
    }

    /**
     * 收藏任务
     */
    public void favoriteTask(Long taskId, Long userId) {
        // 检查任务是否存在
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        favoriteService.addFavorite(userId, "task", taskId);
    }

    /**
     * 取消收藏任务
     */
    public void unfavoriteTask(Long taskId, Long userId) {
        favoriteService.removeFavorite(userId, "task", taskId);
    }

    /**
     * 举报任务
     */
    public Long reportTask(Long taskId, Long userId, Map<String, String> reportData) {
        // 检查任务是否存在
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        // 创建举报请求
        CreateReportRequest request = new CreateReportRequest();
        request.setReportType(4); // 任务举报类型为4
        request.setTargetId(taskId);
        request.setReasonType(Integer.parseInt(reportData.getOrDefault("reasonType", "5"))); // 默认为"其他"
        request.setDescription(reportData.getOrDefault("description", ""));

        return reportService.createReport(userId, request);
    }

    /**
     * 提交任务结果
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitTaskResult(Long taskId, Long userId, SubmitTaskResultRequest request) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        // 检查是否是接单者
        if (task.getAccepterId() == null || !task.getAccepterId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED.getCode(), "只有接单者可以提交结果");
        }

        // 检查任务状态(只有进行中的任务可以提交结果)
        TaskStatus currentStatus = TaskStatus.fromCode(task.getStatus());
        if (currentStatus != TaskStatus.IN_PROGRESS) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(),
                "当前任务状态不允许提交结果,任务状态: " + currentStatus.getName());
        }

        // 检查状态转换是否合法
        if (!currentStatus.canTransitionTo(TaskStatus.SUBMITTED)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(),
                "不允许从状态 " + currentStatus.getName() + " 转换到 " + TaskStatus.SUBMITTED.getName());
        }

        // 更新任务信息
        task.setResultDescription(request.getDescription());
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            // 将附件列表转换为JSON字符串存储
            task.setResultAttachments(String.join(",", request.getAttachments()));
        }
        task.setStatus(TaskStatus.SUBMITTED.getCode());
        task.setSubmittedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        // 记录操作日志
        recordTaskLog(taskId, userId, "submit", currentStatus.getCode(),
            TaskStatus.SUBMITTED.getCode(), "执行者提交任务结果");

        // TODO: 发送通知给发布者审核
    }

    /**
     * 确认任务完成(发布者审核结果)
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmTaskCompletion(Long taskId, Long userId, ConfirmTaskRequest request) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        // 检查是否是发布者
        if (!task.getPublisherId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED.getCode(), "只有发布者可以确认任务完成");
        }

        // 检查任务状态(只有待确认的任务可以审核)
        TaskStatus currentStatus = TaskStatus.fromCode(task.getStatus());
        if (currentStatus != TaskStatus.SUBMITTED) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(),
                "当前任务状态不允许确认,任务状态: " + currentStatus.getName());
        }

        if (request.getApproved()) {
            // 批准完成 - 发放积分
            confirmApproved(task, userId, request.getFeedback());
        } else {
            // 拒绝 - 要求重新提交
            confirmRejected(task, userId, request.getFeedback());
        }
    }

    /**
     * 批准任务完成
     */
    private void confirmApproved(Task task, Long publisherId, String feedback) {
        // 检查状态转换
        TaskStatus currentStatus = TaskStatus.fromCode(task.getStatus());
        if (!currentStatus.canTransitionTo(TaskStatus.COMPLETED)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(),
                "不允许从状态 " + currentStatus.getName() + " 转换到 " + TaskStatus.COMPLETED.getName());
        }

        // 发放积分给接单者
        User accepter = userMapper.selectById(task.getAccepterId());
        if (accepter == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        Integer oldPoints = accepter.getPoints();
        accepter.setPoints(oldPoints + task.getRewardPoints());
        userMapper.updateById(accepter);

        // 记录积分日志
        recordPointsLog(
            accepter.getUId(),
            task.getRewardPoints(),
            accepter.getPoints(),
            "完成任务获得积分：" + task.getTitle(),
            "task",
            task.getTid()
        );

        // 更新任务状态
        task.setStatus(TaskStatus.COMPLETED.getCode());
        task.setCompletedAt(LocalDateTime.now());
        task.setRejectionReason(null); // 清空拒绝原因
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        // 记录操作日志
        recordTaskLog(task.getTid(), publisherId, "confirm", currentStatus.getCode(),
            TaskStatus.COMPLETED.getCode(),
            "发布者确认任务完成" + (feedback != null ? ", 反馈: " + feedback : ""));

        // TODO: 发送通知给接单者
    }

    /**
     * 拒绝任务结果
     */
    private void confirmRejected(Task task, Long publisherId, String feedback) {
        if (feedback == null || feedback.trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "拒绝时必须提供反馈意见");
        }

        // 检查状态转换
        TaskStatus currentStatus = TaskStatus.fromCode(task.getStatus());
        if (!currentStatus.canTransitionTo(TaskStatus.IN_PROGRESS)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(),
                "不允许从状态 " + currentStatus.getName() + " 转换回 " + TaskStatus.IN_PROGRESS.getName());
        }

        // 更新任务状态为进行中,要求重新提交
        task.setStatus(TaskStatus.IN_PROGRESS.getCode());
        task.setRejectionReason(feedback);
        task.setResultDescription(null);
        task.setResultAttachments(null);
        task.setSubmittedAt(null);
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        // 记录操作日志
        recordTaskLog(task.getTid(), publisherId, "reject", currentStatus.getCode(),
            TaskStatus.IN_PROGRESS.getCode(), "发布者拒绝结果并要求重新提交: " + feedback);

        // TODO: 发送通知给接单者
    }

    /**
     * 开始执行任务(接单者开始工作)
     */
    @Transactional(rollbackFor = Exception.class)
    public void startTask(Long taskId, Long userId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        // 检查是否是接单者
        if (task.getAccepterId() == null || !task.getAccepterId().equals(userId)) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED.getCode(), "只有接单者可以开始任务");
        }

        // 检查任务状态(只有已接取的任务可以开始)
        TaskStatus currentStatus = TaskStatus.fromCode(task.getStatus());
        if (currentStatus != TaskStatus.ACCEPTED) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(),
                "当前任务状态不允许开始,任务状态: " + currentStatus.getName());
        }

        // 检查状态转换
        if (!currentStatus.canTransitionTo(TaskStatus.IN_PROGRESS)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(),
                "不允许从状态 " + currentStatus.getName() + " 转换到 " + TaskStatus.IN_PROGRESS.getName());
        }

        // 更新任务状态
        task.setStatus(TaskStatus.IN_PROGRESS.getCode());
        task.setStartedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        // 记录操作日志
        recordTaskLog(taskId, userId, "start", currentStatus.getCode(),
            TaskStatus.IN_PROGRESS.getCode(), "接单者开始执行任务");
    }

    /**
     * 记录任务操作日志
     */
    private void recordTaskLog(Long taskId, Long userId, String action,
                               Integer oldStatus, Integer newStatus, String remark) {
        TaskLog log = new TaskLog();
        log.setTaskId(taskId);
        log.setUserId(userId);
        log.setAction(action);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setRemark(remark);
        log.setCreatedAt(LocalDateTime.now());
        taskLogMapper.insert(log);
    }

    /**
     * 创建任务评价
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createTaskRating(Long taskId, Long userId, CreateRatingRequest request) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        // 检查任务是否已完成
        if (task.getStatus() != TaskStatus.COMPLETED.getCode()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "只有已完成的任务才能评价");
        }

        // 确定评价者和被评价者
        Long ratedId;
        if (task.getPublisherId().equals(userId)) {
            // 发布者评价接单者
            ratedId = task.getAccepterId();
        } else if (task.getAccepterId() != null && task.getAccepterId().equals(userId)) {
            // 接单者评价发布者
            ratedId = task.getPublisherId();
        } else {
            throw new BusinessException(ResultCode.PERMISSION_DENIED.getCode(),
                "只有任务的发布者或接单者可以互相评价");
        }

        // 检查是否已经评价过
        LambdaQueryWrapper<TaskRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskRating::getTaskId, taskId)
               .eq(TaskRating::getRaterId, userId);
        if (taskRatingMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "您已经评价过该任务了");
        }

        // 创建评价记录
        TaskRating rating = new TaskRating();
        rating.setTaskId(taskId);
        rating.setRaterId(userId);
        rating.setRatedId(ratedId);
        rating.setRating(request.getRating());
        rating.setComment(request.getComment());
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            rating.setTags(String.join(",", request.getTags()));
        }
        rating.setCreatedAt(LocalDateTime.now());
        taskRatingMapper.insert(rating);

        // 更新被评价者的信用分
        updateUserCreditScore(ratedId);

        return rating.getRatingId();
    }

    /**
     * 更新用户信用分
     */
    private void updateUserCreditScore(Long userId) {
        Double avgRating = taskRatingMapper.getAverageRating(userId);
        Long ratingCount = taskRatingMapper.getRatingCount(userId);

        User user = userMapper.selectById(userId);
        if (user != null && avgRating != null) {
            // 更新用户信用分和评价次数
            user.setCreditScore(avgRating);
            user.setRatingCount(ratingCount.intValue());
            userMapper.updateById(user);
        }
    }

    /**
     * 获取任务操作日志
     */
    public List<TaskLog> getTaskLogs(Long taskId) {
        LambdaQueryWrapper<TaskLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskLog::getTaskId, taskId)
               .orderByDesc(TaskLog::getCreatedAt);
        return taskLogMapper.selectList(wrapper);
    }

    /**
     * 获取任务评价列表
     */
    public List<TaskRating> getTaskRatings(Long taskId) {
        LambdaQueryWrapper<TaskRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskRating::getTaskId, taskId)
               .orderByDesc(TaskRating::getCreatedAt);
        return taskRatingMapper.selectList(wrapper);
    }
}
