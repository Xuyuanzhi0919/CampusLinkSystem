package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.dto.admin.*;
import com.campuslink.entity.*;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.DigestUtils;

/**
 * 管理员服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;
    private final ResourceMapper resourceMapper;
    private final QuestionMapper questionMapper;
    private final TaskMapper taskMapper;
    private final ActivityMapper activityMapper;
    private final ReportMapper reportMapper;
    private final PointsLogMapper pointsLogMapper;
    private final SchoolMapper schoolMapper;
    private final AnswerMapper answerMapper;
    private final NotificationMapper notificationMapper;

    // ==================== 仪表板 ====================

    public AdminDashboardVO getDashboard() {
        AdminDashboardVO vo = new AdminDashboardVO();

        // 总量统计
        vo.setTotalUsers(userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, 1)));
        vo.setTotalResources(resourceMapper.selectCount(new LambdaQueryWrapper<Resource>().eq(Resource::getStatus, 1)));
        vo.setTotalQuestions(questionMapper.selectCount(new LambdaQueryWrapper<Question>().eq(Question::getStatus, 1)));
        vo.setTotalTasks(taskMapper.selectCount(null));
        vo.setTotalActivities(activityMapper.selectCount(null));

        // 今日新增
        LocalDateTime dayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime dayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        vo.setTodayNewUsers(userMapper.selectCount(
                new LambdaQueryWrapper<User>().between(User::getCreatedAt, dayStart, dayEnd)));
        vo.setTodayNewResources(resourceMapper.selectCount(
                new LambdaQueryWrapper<Resource>().between(Resource::getCreatedAt, dayStart, dayEnd)));
        vo.setTodayNewQuestions(questionMapper.selectCount(
                new LambdaQueryWrapper<Question>().between(Question::getCreatedAt, dayStart, dayEnd)));
        vo.setTodayNewTasks(taskMapper.selectCount(
                new LambdaQueryWrapper<Task>().between(Task::getCreatedAt, dayStart, dayEnd)));

        // 待处理
        vo.setPendingResources(resourceMapper.selectCount(
                new LambdaQueryWrapper<Resource>().eq(Resource::getStatus, 0)));
        vo.setPendingReports(reportMapper.selectCount(
                new LambdaQueryWrapper<Report>().eq(Report::getStatus, 0)));
        vo.setBannedUsers(userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getStatus, 0)));
        vo.setPendingTasks(taskMapper.selectCount(
                new LambdaQueryWrapper<Task>().eq(Task::getStatus, 0)));

        // 近7天趋势
        vo.setUserTrend(buildTrend7Days("user"));
        vo.setContentTrend(buildTrend7Days("content"));

        return vo;
    }

    private List<Map<String, Object>> buildTrend7Days(String type) {
        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
            long count;
            if ("user".equals(type)) {
                count = userMapper.selectCount(
                        new LambdaQueryWrapper<User>().between(User::getCreatedAt, start, end));
            } else {
                long q = questionMapper.selectCount(
                        new LambdaQueryWrapper<Question>().between(Question::getCreatedAt, start, end));
                long r = resourceMapper.selectCount(
                        new LambdaQueryWrapper<Resource>().between(Resource::getCreatedAt, start, end));
                count = q + r;
            }
            Map<String, Object> point = new HashMap<>();
            point.put("date", date.format(fmt));
            point.put("count", count);
            trend.add(point);
        }
        return trend;
    }

    // ==================== 用户管理 ====================

    public PageResult<AdminUserVO> listUsers(AdminUserQueryRequest req) {
        Page<User> page = new Page<>(req.getPage(), req.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(req.getKeyword())) {
            wrapper.and(w -> w
                    .like(User::getUsername, req.getKeyword())
                    .or().like(User::getNickname, req.getKeyword())
                    .or().like(User::getEmail, req.getKeyword())
            );
        }
        if (StringUtils.hasText(req.getRole())) {
            wrapper.eq(User::getRole, req.getRole());
        }
        if (req.getStatus() != null) {
            wrapper.eq(User::getStatus, req.getStatus());
        }
        String sortBy = StringUtils.hasText(req.getSortBy()) ? req.getSortBy() : "createdAt";
        boolean asc = "asc".equalsIgnoreCase(req.getSortOrder());
        switch (sortBy) {
            case "points" -> { if (asc) wrapper.orderByAsc(User::getPoints); else wrapper.orderByDesc(User::getPoints); }
            case "lastLoginTime" -> { if (asc) wrapper.orderByAsc(User::getLastLoginTime); else wrapper.orderByDesc(User::getLastLoginTime); }
            default -> { if (asc) wrapper.orderByAsc(User::getCreatedAt); else wrapper.orderByDesc(User::getCreatedAt); }
        }

        Page<User> result = userMapper.selectPage(page, wrapper);
        List<AdminUserVO> list = result.getRecords().stream().map(this::toAdminUserVO).toList();

        return new PageResult<>(list, result.getTotal(),
                (long) req.getPage(), (long) req.getPageSize(),
                result.getPages());
    }

    public AdminUserVO getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        return toAdminUserVO(user);
    }

    @Transactional
    public void banUser(Long userId, BanUserRequest req) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        user.setStatus(req.getStatus());
        userMapper.updateById(user);
        log.info("管理员操作用户状态 - userId: {}, status: {}, reason: {}", userId, req.getStatus(), req.getReason());
    }

    @Transactional
    public void setRole(Long userId, SetRoleRequest req) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        user.setRole(req.getRole());
        userMapper.updateById(user);
        log.info("管理员修改用户角色 - userId: {}, role: {}", userId, req.getRole());
    }

    @Transactional
    public void updateUserInfo(Long userId, AdminUpdateUserInfoRequest req) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        if (StringUtils.hasText(req.getNickname())) user.setNickname(req.getNickname());
        if (StringUtils.hasText(req.getEmail()))    user.setEmail(req.getEmail());
        if (StringUtils.hasText(req.getPhone()))    user.setPhone(req.getPhone());
        if (StringUtils.hasText(req.getMajor()))    user.setMajor(req.getMajor());
        if (StringUtils.hasText(req.getStudentId())) user.setStudentId(req.getStudentId());
        if (req.getGrade() != null)                 user.setGrade(req.getGrade());
        userMapper.updateById(user);
        log.info("管理员修改用户信息 - userId: {}", userId);
    }

    @Transactional
    public void adjustPoints(Long operatorId, Long userId, AdjustPointsRequest req) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");

        int newPoints = Math.max(0, user.getPoints() + req.getDelta());
        user.setPoints(newPoints);
        userMapper.updateById(user);

        // 记录积分流水
        PointsLog log2 = new PointsLog();
        log2.setUserId(userId);
        log2.setPointsChange(req.getDelta());
        log2.setPointsAfter(newPoints);
        log2.setRelatedType("admin_adjust");
        log2.setReason(StringUtils.hasText(req.getReason()) ? req.getReason() : "管理员手动调整");
        log2.setCreatedAt(LocalDateTime.now());
        pointsLogMapper.insert(log2);

        log.info("管理员调整积分 - operator: {}, userId: {}, delta: {}", operatorId, userId, req.getDelta());
    }

    // ==================== 批量操作 ====================

    @Transactional
    public int batchSetStatus(BatchStatusRequest req) {
        if (req.getUserIds() == null || req.getUserIds().isEmpty()) return 0;
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<User>()
                .set(User::getStatus, req.getStatus())
                .in(User::getUId, req.getUserIds());
        int count = userMapper.update(null, wrapper);
        log.info("管理员批量操作用户状态 - count: {}, status: {}", count, req.getStatus());
        return count;
    }

    // ==================== 用户内容统计 ====================

    public Map<String, Long> getUserStats(Long userId) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("resources", resourceMapper.selectCount(
                new LambdaQueryWrapper<Resource>().eq(Resource::getUploaderId, userId)));
        stats.put("questions", questionMapper.selectCount(
                new LambdaQueryWrapper<Question>().eq(Question::getAskerId, userId)));
        stats.put("answers", answerMapper.selectCount(
                new LambdaQueryWrapper<Answer>().eq(Answer::getResponderId, userId)));
        stats.put("tasks", taskMapper.selectCount(
                new LambdaQueryWrapper<Task>().eq(Task::getPublisherId, userId)));
        return stats;
    }

    // ==================== 密码重置 ====================

    @Transactional
    public Map<String, String> resetPassword(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        String newPassword = "CL" + String.format("%06d", new java.util.Random().nextInt(1000000));
        user.setPasswordHash(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        userMapper.updateById(user);
        log.info("管理员重置用户密码 - userId: {}", userId);
        Map<String, String> result = new HashMap<>();
        result.put("newPassword", newPassword);
        return result;
    }

    // ==================== 内容管理 ====================

    public PageResult<Resource> listResources(AdminContentQueryRequest req) {
        Page<Resource> page = new Page<>(req.getPage(), req.getPageSize());
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(req.getKeyword())) {
            wrapper.like(Resource::getTitle, req.getKeyword());
        }
        if (req.getStatus() != null) {
            wrapper.eq(Resource::getStatus, req.getStatus());
        }
        wrapper.orderByDesc(Resource::getCreatedAt);
        Page<Resource> result = resourceMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(),
                (long) req.getPage(), (long) req.getPageSize(), result.getPages());
    }

    @Transactional
    public void updateResourceStatus(Long resourceId, UpdateContentStatusRequest req) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) throw new BusinessException(404, "资源不存在");
        resource.setStatus(req.getStatus());
        if (StringUtils.hasText(req.getReason())) {
            resource.setRejectReason(req.getReason());
        }
        resourceMapper.updateById(resource);
    }

    public PageResult<Question> listQuestions(AdminContentQueryRequest req) {
        Page<Question> page = new Page<>(req.getPage(), req.getPageSize());
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(req.getKeyword())) {
            wrapper.like(Question::getTitle, req.getKeyword());
        }
        if (req.getStatus() != null) {
            wrapper.eq(Question::getStatus, req.getStatus());
        }
        wrapper.orderByDesc(Question::getCreatedAt);
        Page<Question> result = questionMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(),
                (long) req.getPage(), (long) req.getPageSize(), result.getPages());
    }

    @Transactional
    public void updateQuestionStatus(Long questionId, UpdateContentStatusRequest req) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) throw new BusinessException(404, "问题不存在");
        question.setStatus(req.getStatus());
        questionMapper.updateById(question);
    }

    public PageResult<Answer> listAnswers(AdminContentQueryRequest req) {
        Page<Answer> page = new Page<>(req.getPage(), req.getPageSize());
        LambdaQueryWrapper<Answer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(req.getKeyword())) {
            wrapper.like(Answer::getContent, req.getKeyword());
        }
        if (req.getStatus() != null) {
            wrapper.eq(Answer::getStatus, req.getStatus());
        }
        wrapper.orderByDesc(Answer::getCreatedAt);
        Page<Answer> result = answerMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(),
                (long) req.getPage(), (long) req.getPageSize(), result.getPages());
    }

    @Transactional
    public void updateAnswerStatus(Long answerId, UpdateContentStatusRequest req) {
        Answer answer = answerMapper.selectById(answerId);
        if (answer == null) throw new BusinessException(404, "回答不存在");
        answer.setStatus(req.getStatus());
        answerMapper.updateById(answer);
    }

    // ==================== 公告历史 ====================

    public List<Notification> getNoticeHistory() {
        List<Notification> all = notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .in(Notification::getNotifyType, "announcement", "system", "warning")
                        .isNull(Notification::getRelatedId)
                        .orderByDesc(Notification::getCreatedAt)
                        .last("LIMIT 200")
        );
        Map<String, Notification> seen = new LinkedHashMap<>();
        for (Notification n : all) {
            String key = n.getTitle() + ":" + n.getNotifyType();
            seen.putIfAbsent(key, n);
            if (seen.size() >= 20) break;
        }
        return new ArrayList<>(seen.values());
    }

    // ==================== 内部工具 ====================

    private AdminUserVO toAdminUserVO(User user) {
        AdminUserVO vo = new AdminUserVO();
        BeanUtils.copyProperties(user, vo);
        // 查学校名称
        if (user.getSchoolId() != null) {
            School school = schoolMapper.selectById(user.getSchoolId());
            if (school != null) vo.setSchoolName(school.getSchoolName());
        }
        return vo;
    }
}
