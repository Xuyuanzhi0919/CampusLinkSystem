package com.campuslink.service;

import com.campuslink.common.ResultCode;
import com.campuslink.dto.task.PublishTaskRequest;
import com.campuslink.entity.Task;
import com.campuslink.entity.User;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TaskService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskMapper taskMapper;
    @Mock private UserMapper userMapper;
    @Mock private PointsLogMapper pointsLogMapper;
    @Mock private FavoriteService favoriteService;
    @Mock private ReportService reportService;
    @Mock private TaskLogMapper taskLogMapper;
    @Mock private TaskRatingMapper taskRatingMapper;
    @Mock private NotificationService notificationService;
    @Mock private LevelService levelService;
    @Mock private SystemConfigMapper systemConfigMapper;

    @InjectMocks
    private TaskService taskService;

    private User publisher;

    @BeforeEach
    void setUp() {
        publisher = new User();
        publisher.setUId(1L);
        publisher.setNickname("发布者");
        publisher.setPoints(500);
        publisher.setLevel(2);
    }

    private PublishTaskRequest buildRequest(String title, int rewardPoints) {
        PublishTaskRequest req = new PublishTaskRequest();
        req.setTitle(title);
        req.setContent("任务内容");
        req.setTaskType("errand");
        req.setRewardPoints(rewardPoints);
        req.setLocation("图书馆");
        req.setDeadline(LocalDateTime.now().plusDays(1));
        return req;
    }

    // ─────────────── publishTask ───────────────

    @Test
    @DisplayName("publishTask: 用户不存在时抛出 USER_NOT_FOUND")
    void publishTask_whenUserNotFound_throwsException() {
        when(userMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> taskService.publishTask(99L, buildRequest("测试任务", 10)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ResultCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("publishTask: 积分不足时抛出 INSUFFICIENT_POINTS")
    void publishTask_whenInsufficientPoints_throwsException() {
        publisher.setPoints(5); // 只有5积分
        when(userMapper.selectById(1L)).thenReturn(publisher);
        // 系统配置返回 null，使用默认上限 100
        when(systemConfigMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> taskService.publishTask(1L, buildRequest("测试任务", 50)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ResultCode.INSUFFICIENT_POINTS.getMessage());
    }

    @Test
    @DisplayName("publishTask: 积分足够时成功发布并扣除积分")
    void publishTask_whenSufficientPoints_deductsPointsAndCreatesTask() {
        when(userMapper.selectById(1L)).thenReturn(publisher);
        when(systemConfigMapper.selectOne(any())).thenReturn(null); // 使用默认上限100
        when(taskMapper.insert(any(Task.class))).thenReturn(1);
        when(userMapper.updateById(any())).thenReturn(1);
        when(pointsLogMapper.insert(any())).thenReturn(1);

        taskService.publishTask(1L, buildRequest("帮我打印课件", 50));

        // 验证积分被扣除（updateById 被调用一次更新积分）
        verify(userMapper, times(1)).updateById(argThat(u -> ((User) u).getPoints() == 450));
        // 验证任务被插入
        verify(taskMapper, times(1)).insert(any(Task.class));
    }

    // ─────────────── acceptTask ───────────────

    @Test
    @DisplayName("acceptTask: 任务不存在时抛出 TASK_NOT_FOUND")
    void acceptTask_whenTaskNotFound_throwsException() {
        when(taskMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> taskService.acceptTask(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ResultCode.TASK_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("acceptTask: 任务已被接单时抛出 TASK_ALREADY_ACCEPTED")
    void acceptTask_whenTaskAlreadyAccepted_throwsException() {
        Task task = new Task();
        task.setTid(1L);
        task.setPublisherId(2L);
        task.setStatus(2); // 已在进行中

        when(taskMapper.selectById(1L)).thenReturn(task);

        assertThatThrownBy(() -> taskService.acceptTask(3L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ResultCode.TASK_ALREADY_ACCEPTED.getMessage());
    }

    @Test
    @DisplayName("acceptTask: 不能接受自己发布的任务")
    void acceptTask_ownTask_throwsException() {
        Task task = new Task();
        task.setTid(1L);
        task.setPublisherId(1L); // 发布者 ID = 1
        task.setStatus(0); // 待接单

        when(taskMapper.selectById(1L)).thenReturn(task);

        // 用户 1 试图接自己发布的任务
        assertThatThrownBy(() -> taskService.acceptTask(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ResultCode.CANNOT_ACCEPT_OWN_TASK.getMessage());
    }
}
