package com.campuslink.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.entity.Task;
import com.campuslink.entity.TaskLog;
import com.campuslink.entity.User;
import com.campuslink.enums.TaskStatus;
import com.campuslink.mapper.TaskLogMapper;
import com.campuslink.mapper.TaskMapper;
import com.campuslink.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务超时处理定时调度器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskExpirationScheduler {
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final TaskLogMapper taskLogMapper;

    /**
     * 每小时检查超时任务
     * cron表达式: 每小时的第0分钟执行
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void checkExpiredTasks() {
        log.info("开始检查超时任务...");

        LocalDateTime now = LocalDateTime.now();

        // 查询所有超时的活动任务(待接单、已接取、进行中、待确认)
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(Task::getDeadline, now)
               .in(Task::getStatus,
                   TaskStatus.ACTIVE.getCode(),
                   TaskStatus.ACCEPTED.getCode(),
                   TaskStatus.IN_PROGRESS.getCode(),
                   TaskStatus.SUBMITTED.getCode());

        List<Task> expiredTasks = taskMapper.selectList(wrapper);

        if (expiredTasks.isEmpty()) {
            log.info("没有发现超时任务");
            return;
        }

        log.info("发现 {} 个超时任务,开始处理...", expiredTasks.size());

        int processedCount = 0;
        for (Task task : expiredTasks) {
            try {
                handleExpiredTask(task);
                processedCount++;
            } catch (Exception e) {
                log.error("处理超时任务失败: taskId={}, error={}", task.getTid(), e.getMessage(), e);
            }
        }

        log.info("超时任务处理完成: 总数={}, 成功={}", expiredTasks.size(), processedCount);
    }

    /**
     * 处理单个超时任务
     */
    private void handleExpiredTask(Task task) {
        Integer oldStatus = task.getStatus();
        TaskStatus oldTaskStatus = TaskStatus.fromCode(oldStatus);

        log.info("处理超时任务: taskId={}, title={}, oldStatus={}, deadline={}",
            task.getTid(), task.getTitle(), oldTaskStatus.getName(), task.getDeadline());

        // 如果任务还未被接单,退还发布者积分
        if (oldStatus == TaskStatus.ACTIVE.getCode()) {
            refundPublisherPoints(task);
        }
        // 如果任务已被接单但未完成,不扣除接单者积分(可能是发布者原因)
        // 但可以记录到信用评分中

        // 更新任务状态为已超时
        task.setStatus(TaskStatus.EXPIRED.getCode());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        // 记录操作日志
        recordTaskLog(
            task.getTid(),
            0L, // 系统操作,userId为0
            "expire",
            oldStatus,
            TaskStatus.EXPIRED.getCode(),
            "任务超时自动处理: 原状态=" + oldTaskStatus.getName()
        );

        log.info("超时任务处理完成: taskId={}, 积分{}",
            task.getTid(),
            oldStatus == TaskStatus.ACTIVE.getCode() ? "已退还" : "无需退还");

        // TODO: 发送通知给相关用户
    }

    /**
     * 退还发布者积分
     */
    private void refundPublisherPoints(Task task) {
        User publisher = userMapper.selectById(task.getPublisherId());
        if (publisher != null) {
            Integer oldPoints = publisher.getPoints();
            publisher.setPoints(oldPoints + task.getRewardPoints());
            userMapper.updateById(publisher);

            log.info("退还发布者积分: userId={}, points={}, taskId={}",
                publisher.getUId(), task.getRewardPoints(), task.getTid());

            // TODO: 记录积分日志
        }
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
     * 每天凌晨1点清理过期的任务日志(保留近30天)
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void cleanupOldTaskLogs() {
        log.info("开始清理过期任务日志...");

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        LambdaQueryWrapper<TaskLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(TaskLog::getCreatedAt, thirtyDaysAgo);

        int deletedCount = taskLogMapper.delete(wrapper);

        log.info("任务日志清理完成: 删除 {} 条记录", deletedCount);
    }
}
