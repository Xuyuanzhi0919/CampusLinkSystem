package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.task.*;
import com.campuslink.entity.PointsLog;
import com.campuslink.entity.Task;
import com.campuslink.entity.User;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.PointsLogMapper;
import com.campuslink.mapper.TaskMapper;
import com.campuslink.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务服务
 */
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final PointsLogMapper pointsLogMapper;

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
    public TaskResponse getTaskById(Long taskId) {
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

        // 更新任务状态
        task.setAccepterId(userId);
        task.setStatus(1); // 进行中
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
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

        // 检查任务状态
        if (task.getStatus() != 1) {
            throw new BusinessException(ResultCode.TASK_NOT_IN_PROGRESS);
        }

        // 更新任务状态
        task.setStatus(2); // 已完成
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
            }
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
}
