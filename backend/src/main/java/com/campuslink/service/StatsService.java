package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.dto.stats.TodayStatsResponse;
import com.campuslink.entity.Question;
import com.campuslink.entity.Resource;
import com.campuslink.entity.Task;
import com.campuslink.entity.User;
import com.campuslink.mapper.QuestionMapper;
import com.campuslink.mapper.ResourceMapper;
import com.campuslink.mapper.TaskMapper;
import com.campuslink.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 统计服务类
 * 提供今日活跃数据、平台统计等功能
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StatsService {
    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;
    private final ResourceMapper resourceMapper;
    private final TaskMapper taskMapper;

    /**
     * 获取平台数据统计
     * 包含：注册用户总数、今日新增问答、今日资源上传
     * 使用缓存，缓存键包含日期，10分钟过期
     *
     * @return 平台统计数据
     */
    @Cacheable(value = "todayStats", key = "T(java.time.LocalDate).now().toString()", unless = "#result == null")
    public TodayStatsResponse getTodayStats() {
        // 获取今天的开始和结束时间
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        // 统计注册用户总数（改为显示平台规模，而非今日登录数）
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getStatus, 1); // 只统计正常状态的用户
        Integer activeUsers = Math.toIntExact(userMapper.selectCount(userWrapper));

        // 统计今日新增问题数
        LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.ge(Question::getCreatedAt, startOfDay);
        questionWrapper.le(Question::getCreatedAt, endOfDay);
        questionWrapper.eq(Question::getStatus, 1); // 只统计正常状态
        Integer newQuestions = Math.toIntExact(questionMapper.selectCount(questionWrapper));

        // 统计今日资源上传数
        LambdaQueryWrapper<Resource> resourceWrapper = new LambdaQueryWrapper<>();
        resourceWrapper.ge(Resource::getCreatedAt, startOfDay);
        resourceWrapper.le(Resource::getCreatedAt, endOfDay);
        resourceWrapper.in(Resource::getStatus, 0, 1); // 待审核和已通过
        Integer newResources = Math.toIntExact(resourceMapper.selectCount(resourceWrapper));

        // 统计今日新增任务数
        LambdaQueryWrapper<Task> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.ge(Task::getCreatedAt, startOfDay);
        taskWrapper.le(Task::getCreatedAt, endOfDay);
        Integer newTasks = Math.toIntExact(taskMapper.selectCount(taskWrapper));

        // 今日活动参与数（这里先设置为0，后续可以从activity_participant表查询）
        Integer activityParticipants = 0;

        TodayStatsResponse response = new TodayStatsResponse();
        response.setActiveUsers(activeUsers);
        response.setNewQuestions(newQuestions);
        response.setNewResources(newResources);
        response.setNewTasks(newTasks);
        response.setActivityParticipants(activityParticipants);

        log.info("今日统计 - 注册用户: {}, 新增问答: {}, 资源上传: {}, 新增任务: {}",
                activeUsers, newQuestions, newResources, newTasks);

        return response;
    }

    /**
     * 获取平台总体统计（可扩展）
     *
     * @return 总体统计数据
     */
    public TodayStatsResponse getTotalStats() {
        // 统计所有用户数
        Integer totalUsers = Math.toIntExact(userMapper.selectCount(null));

        // 统计所有问题数
        LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.eq(Question::getStatus, 1);
        Integer totalQuestions = Math.toIntExact(questionMapper.selectCount(questionWrapper));

        // 统计所有资源数
        LambdaQueryWrapper<Resource> resourceWrapper = new LambdaQueryWrapper<>();
        resourceWrapper.eq(Resource::getStatus, 1);
        Integer totalResources = Math.toIntExact(resourceMapper.selectCount(resourceWrapper));

        // 统计所有任务数
        Integer totalTasks = Math.toIntExact(taskMapper.selectCount(null));

        TodayStatsResponse response = new TodayStatsResponse();
        response.setActiveUsers(totalUsers);
        response.setNewQuestions(totalQuestions);
        response.setNewResources(totalResources);
        response.setNewTasks(totalTasks);
        response.setActivityParticipants(0);

        return response;
    }
}
