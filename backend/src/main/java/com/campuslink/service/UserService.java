package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.*;
import com.campuslink.dto.user.UpdateProfileRequest;
import com.campuslink.entity.PointsLog;
import com.campuslink.entity.School;
import com.campuslink.entity.User;
import com.campuslink.exception.BusinessException;
import com.campuslink.entity.SystemConfig;
import com.campuslink.mapper.PointsLogMapper;
import com.campuslink.mapper.SchoolMapper;
import com.campuslink.mapper.SystemConfigMapper;
import com.campuslink.mapper.UserMapper;
import com.campuslink.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PointsLogMapper pointsLogMapper;
    private final SchoolMapper schoolMapper;
    private final JwtUtil jwtUtil;
    private final EmailCodeService emailCodeService;

    // 统计数据查询所需Mapper
    private final com.campuslink.mapper.ResourceMapper resourceMapper;
    private final com.campuslink.mapper.DownloadLogMapper downloadLogMapper;
    private final com.campuslink.mapper.QuestionMapper questionMapper;
    private final com.campuslink.mapper.AnswerMapper answerMapper;
    private final com.campuslink.mapper.TaskMapper taskMapper;
    private final com.campuslink.mapper.FavoriteMapper favoriteMapper;
    private final SystemConfigMapper systemConfigMapper;

    /**
     * 用户注册
     */
    public AuthResponse register(RegisterRequest request) {
        // 验证邮箱验证码
        emailCodeService.verify(request.getEmail(), "register", request.getCode());

        // 检查用户名是否已存在
        LambdaQueryWrapper<User> usernameQuery = new LambdaQueryWrapper<>();
        usernameQuery.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(usernameQuery) > 0) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        // 检查邮箱是否已存在
        LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
        emailQuery.eq(User::getEmail, request.getEmail());
        if (userMapper.selectCount(emailQuery) > 0) {
            throw new BusinessException(400, "该邮箱已被注册");
        }

        // 检查手机号是否已存在（如果提供）
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            LambdaQueryWrapper<User> phoneQuery = new LambdaQueryWrapper<>();
            phoneQuery.eq(User::getPhone, request.getPhone());
            if (userMapper.selectCount(phoneQuery) > 0) {
                throw new BusinessException(400, "该手机号已被注册");
            }
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(encryptPassword(request.getPassword())); // 加密密码
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setSchoolId(request.getSchoolId());
        user.setRole(request.getRole() != null ? request.getRole() : "student");
        user.setPoints(100); // 注册奖励100积分
        user.setLevel(1);
        user.setStatus(1); // 1-正常
        user.setIsVerified(0); // 0-未认证

        userMapper.insert(user);

        log.info("用户注册成功: {}", user.getUsername());

        // 生成 Token
        String token = jwtUtil.generateToken(user.getUId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUId());

        // 返回用户信息
        UserVO userVO = convertToVO(user);
        return new AuthResponse(token, refreshToken, userVO);
    }

    /**
     * 用户登录
     */
    public AuthResponse login(LoginRequest request) {
        // 查询用户（支持用户名、邮箱、手机号登录）
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.and(wrapper -> wrapper
                .eq(User::getUsername, request.getAccount())
                .or()
                .eq(User::getEmail, request.getAccount())
                .or()
                .eq(User::getPhone, request.getAccount())
        );

        User user = userMapper.selectOne(query);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证密码
        if (!encryptPassword(request.getPassword()).equals(user.getPasswordHash())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 检查账号状态（1-正常，0-禁用）
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户登录成功: {}", user.getUsername());

        // 生成 Token
        String token = jwtUtil.generateToken(user.getUId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUId());

        // 返回用户信息
        UserVO userVO = convertToVO(user);
        return new AuthResponse(token, refreshToken, userVO);
    }

    /**
     * 根据 ID 获取用户信息
     */
    public UserVO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return convertToVO(user);
    }

    /**
     * 刷新 Token
     */
    public AuthResponse refreshToken(String refreshToken) {
        try {
            // 验证 RefreshToken
            if (!jwtUtil.validateToken(refreshToken)) {
                throw new BusinessException(ResultCode.TOKEN_INVALID);
            }

            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            User user = userMapper.selectById(userId);

            if (user == null) {
                throw new BusinessException(ResultCode.USER_NOT_FOUND);
            }

            // 生成新的 Token
            String newToken = jwtUtil.generateToken(user.getUId(), user.getUsername(), user.getRole());
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getUId());

            UserVO userVO = convertToVO(user);
            return new AuthResponse(newToken, newRefreshToken, userVO);

        } catch (Exception e) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
    }

    /**
     * 密码加密（使用 MD5）
     */
    private String encryptPassword(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }

    /**
     * 获取用户个人资料
     */
    public UserVO getUserProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return convertToVO(user);
    }

    /**
     * 获取用户统计数据
     */
    public UserStatsVO getUserStats(Long userId) {
        // 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 查询上传资源数
        LambdaQueryWrapper<com.campuslink.entity.Resource> resourceQuery = new LambdaQueryWrapper<>();
        resourceQuery.eq(com.campuslink.entity.Resource::getUploaderId, userId);
        int resourceCount = Math.toIntExact(resourceMapper.selectCount(resourceQuery));

        // 查询下载次数
        LambdaQueryWrapper<com.campuslink.entity.DownloadLog> downloadQuery = new LambdaQueryWrapper<>();
        downloadQuery.eq(com.campuslink.entity.DownloadLog::getUserId, userId);
        int downloadCount = Math.toIntExact(downloadLogMapper.selectCount(downloadQuery));

        // 查询提问数
        LambdaQueryWrapper<com.campuslink.entity.Question> questionQuery = new LambdaQueryWrapper<>();
        questionQuery.eq(com.campuslink.entity.Question::getAskerId, userId);
        int questionCount = Math.toIntExact(questionMapper.selectCount(questionQuery));

        // 查询回答数
        LambdaQueryWrapper<com.campuslink.entity.Answer> answerQuery = new LambdaQueryWrapper<>();
        answerQuery.eq(com.campuslink.entity.Answer::getResponderId, userId);
        int answerCount = Math.toIntExact(answerMapper.selectCount(answerQuery));

        // 查询被采纳答案数
        LambdaQueryWrapper<com.campuslink.entity.Answer> acceptedQuery = new LambdaQueryWrapper<>();
        acceptedQuery.eq(com.campuslink.entity.Answer::getResponderId, userId);
        acceptedQuery.eq(com.campuslink.entity.Answer::getIsAccepted, true);
        int acceptedAnswerCount = Math.toIntExact(answerMapper.selectCount(acceptedQuery));

        // 查询发布任务数
        LambdaQueryWrapper<com.campuslink.entity.Task> taskPublishQuery = new LambdaQueryWrapper<>();
        taskPublishQuery.eq(com.campuslink.entity.Task::getPublisherId, userId);
        int taskPublishCount = Math.toIntExact(taskMapper.selectCount(taskPublishQuery));

        // 查询接取任务总数(作为接单者且状态为进行中或已完成)
        LambdaQueryWrapper<com.campuslink.entity.Task> taskAcceptedQuery = new LambdaQueryWrapper<>();
        taskAcceptedQuery.eq(com.campuslink.entity.Task::getAccepterId, userId);
        taskAcceptedQuery.in(com.campuslink.entity.Task::getStatus, 1, 2); // 状态1=进行中, 2=已完成
        int taskAcceptedCount = Math.toIntExact(taskMapper.selectCount(taskAcceptedQuery));

        // 查询完成任务数(作为接单者且状态为已完成)
        LambdaQueryWrapper<com.campuslink.entity.Task> taskCompleteQuery = new LambdaQueryWrapper<>();
        taskCompleteQuery.eq(com.campuslink.entity.Task::getAccepterId, userId);
        taskCompleteQuery.eq(com.campuslink.entity.Task::getStatus, 2); // 状态2=已完成
        int taskCompleteCount = Math.toIntExact(taskMapper.selectCount(taskCompleteQuery));

        // 查询收藏数
        LambdaQueryWrapper<com.campuslink.entity.Favorite> favoriteQuery = new LambdaQueryWrapper<>();
        favoriteQuery.eq(com.campuslink.entity.Favorite::getUserId, userId);
        int favoriteCount = Math.toIntExact(favoriteMapper.selectCount(favoriteQuery));

        // 查询获赞数：资源点赞总和 + 回答点赞总和
        LambdaQueryWrapper<com.campuslink.entity.Resource> resourceLikeQuery = new LambdaQueryWrapper<>();
        resourceLikeQuery.eq(com.campuslink.entity.Resource::getUploaderId, userId)
                .select(com.campuslink.entity.Resource::getLikes);
        List<com.campuslink.entity.Resource> myResources = resourceMapper.selectList(resourceLikeQuery);
        int resourceLikes = myResources.stream()
                .mapToInt(r -> r.getLikes() == null ? 0 : r.getLikes()).sum();

        LambdaQueryWrapper<com.campuslink.entity.Answer> answerLikeQuery = new LambdaQueryWrapper<>();
        answerLikeQuery.eq(com.campuslink.entity.Answer::getResponderId, userId)
                .select(com.campuslink.entity.Answer::getLikes);
        List<com.campuslink.entity.Answer> myAnswerLikes = answerMapper.selectList(answerLikeQuery);
        int answerLikes = myAnswerLikes.stream()
                .mapToInt(a -> a.getLikes() == null ? 0 : a.getLikes()).sum();

        int likeCount = resourceLikes + answerLikes;

        // 计算连续签到天数
        int checkInDays = calculateConsecutiveDays(userId);

        return UserStatsVO.builder()
                .resourceCount(resourceCount)
                .downloadCount(downloadCount)
                .questionCount(questionCount)
                .answerCount(answerCount)
                .acceptedAnswerCount(acceptedAnswerCount)
                .taskPublishCount(taskPublishCount)
                .taskAcceptedCount(taskAcceptedCount)
                .taskCompleteCount(taskCompleteCount)
                .favoriteCount(favoriteCount)
                .likeCount(likeCount)
                .checkInDays(checkInDays)
                .build();
    }

    /**
     * 更新个人资料
     */
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 如果要更新手机号，检查是否已被其他用户使用
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (!request.getPhone().equals(user.getPhone())) {
                LambdaQueryWrapper<User> phoneQuery = new LambdaQueryWrapper<>();
                phoneQuery.eq(User::getPhone, request.getPhone());
                phoneQuery.ne(User::getUId, userId);
                if (userMapper.selectCount(phoneQuery) > 0) {
                    throw new BusinessException(400, "该手机号已被其他用户使用");
                }
            }
        }

        // 更新字段（只更新非空字段）
        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getStudentId() != null && !request.getStudentId().isEmpty()) {
            user.setStudentId(request.getStudentId());
        }
        if (request.getMajor() != null && !request.getMajor().isEmpty()) {
            user.setMajor(request.getMajor());
        }
        if (request.getGrade() != null) {
            user.setGrade(request.getGrade());
        }
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            user.setPhone(request.getPhone());
        }
        if (request.getSchoolId() != null) {
            // 验证学校存在
            School school = schoolMapper.selectById(request.getSchoolId());
            if (school == null) {
                throw new BusinessException(400, "学校不存在");
            }
            user.setSchoolId(request.getSchoolId());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户资料更新成功: userId={}", userId);

        return convertToVO(user);
    }

    /**
     * 获取积分记录列表
     */
    public PageResult<PointsLogVO> getPointsLog(Long userId, Integer page, Integer pageSize) {
        // 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 构建查询条件
        LambdaQueryWrapper<PointsLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsLog::getUserId, userId);
        wrapper.orderByDesc(PointsLog::getCreatedAt);

        // 分页查询
        Page<PointsLog> pageObj = new Page<>(page, pageSize);
        Page<PointsLog> result = pointsLogMapper.selectPage(pageObj, wrapper);

        // 转换为VO对象
        List<PointsLogVO> voList = new ArrayList<>();
        for (PointsLog log : result.getRecords()) {
            PointsLogVO vo = new PointsLogVO();
            BeanUtils.copyProperties(log, vo);
            voList.add(vo);
        }

        // 构建分页结果
        PageResult<PointsLogVO> pageResult = new PageResult<>();
        pageResult.setList(voList);
        pageResult.setTotal(result.getTotal());
        pageResult.setPage((long) page);
        pageResult.setPageSize((long) pageSize);
        pageResult.setTotalPages((result.getTotal() + pageSize - 1) / pageSize);

        return pageResult;
    }

    /**
     * 获取签到状态
     */
    public Boolean getCheckInStatus(Long userId) {
        // 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 检查今日是否已签到
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        LambdaQueryWrapper<PointsLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsLog::getUserId, userId);
        wrapper.eq(PointsLog::getReason, "每日签到");
        wrapper.between(PointsLog::getCreatedAt, todayStart, todayEnd);
        Long count = pointsLogMapper.selectCount(wrapper);

        return count > 0;
    }

    /**
     * 每日签到
     */
    @Transactional(rollbackFor = Exception.class)
    public CheckInResponse checkIn(Long userId) {
        // 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 检查今日是否已签到
        if (getCheckInStatus(userId)) {
            // 今日已签到
            return CheckInResponse.builder()
                    .success(false)
                    .message("今日已签到")
                    .pointsEarned(0)
                    .totalPoints(user.getPoints())
                    .consecutiveDays(0)
                    .checkInTime(LocalDateTime.now())
                    .build();
        }

        // 签到奖励积分：从系统配置读取，默认 10
        SystemConfig signinConfig = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, "points.daily_signin"));
        final int CHECK_IN_POINTS = (signinConfig != null && signinConfig.getConfigValue() != null)
                ? Integer.parseInt(signinConfig.getConfigValue()) : 10;

        // 更新用户积分
        user.setPoints(user.getPoints() + CHECK_IN_POINTS);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 记录积分变化
        PointsLog pointsLog = new PointsLog();
        pointsLog.setUserId(userId);
        pointsLog.setPointsChange(CHECK_IN_POINTS);
        pointsLog.setPointsAfter(user.getPoints());
        pointsLog.setReason("每日签到");
        pointsLog.setRelatedType("check_in");
        pointsLog.setRelatedId(userId);
        pointsLog.setCreatedAt(LocalDateTime.now());
        pointsLogMapper.insert(pointsLog);

        // 计算连续签到天数(简化版:只计算最近的连续天数)
        int consecutiveDays = calculateConsecutiveDays(userId);

        log.info("用户签到成功: userId={}, points={}", userId, user.getPoints());

        return CheckInResponse.builder()
                .success(true)
                .message("签到成功!")
                .pointsEarned(CHECK_IN_POINTS)
                .totalPoints(user.getPoints())
                .consecutiveDays(consecutiveDays)
                .checkInTime(LocalDateTime.now())
                .build();
    }

    /**
     * 获取用户内容被点赞列表（资源 + 回答，按点赞数降序，分页）
     */
    public PageResult<com.campuslink.dto.LikedItemVO> getLikedItems(Long userId, int page, int pageSize) {
        List<com.campuslink.dto.LikedItemVO> all = new ArrayList<>();

        // 资源（likes > 0）
        LambdaQueryWrapper<com.campuslink.entity.Resource> resQuery = new LambdaQueryWrapper<>();
        resQuery.eq(com.campuslink.entity.Resource::getUploaderId, userId)
                .gt(com.campuslink.entity.Resource::getLikes, 0)
                .orderByDesc(com.campuslink.entity.Resource::getLikes);
        List<com.campuslink.entity.Resource> resources = resourceMapper.selectList(resQuery);
        for (com.campuslink.entity.Resource r : resources) {
            all.add(com.campuslink.dto.LikedItemVO.builder()
                    .type("resource")
                    .targetId(r.getRid())
                    .title(r.getTitle())
                    .likes(r.getLikes() == null ? 0 : r.getLikes())
                    .createdAt(r.getCreatedAt())
                    .build());
        }

        // 回答（likes > 0），同时查关联问题标题
        LambdaQueryWrapper<com.campuslink.entity.Answer> ansQuery = new LambdaQueryWrapper<>();
        ansQuery.eq(com.campuslink.entity.Answer::getResponderId, userId)
                .gt(com.campuslink.entity.Answer::getLikes, 0)
                .orderByDesc(com.campuslink.entity.Answer::getLikes);
        List<com.campuslink.entity.Answer> answers = answerMapper.selectList(ansQuery);
        for (com.campuslink.entity.Answer a : answers) {
            String questionTitle = null;
            if (a.getQuestionId() != null) {
                com.campuslink.entity.Question q = questionMapper.selectById(a.getQuestionId());
                if (q != null) questionTitle = q.getTitle();
            }
            // 回答内容摘要：取前60字
            String excerpt = a.getContent() == null ? "" :
                    (a.getContent().length() > 60 ? a.getContent().substring(0, 60) + "…" : a.getContent());
            all.add(com.campuslink.dto.LikedItemVO.builder()
                    .type("answer")
                    .targetId(a.getAid())
                    .title(excerpt)
                    .questionId(a.getQuestionId())
                    .questionTitle(questionTitle)
                    .likes(a.getLikes() == null ? 0 : a.getLikes())
                    .createdAt(a.getCreatedAt())
                    .build());
        }

        // 按点赞数降序整体排序
        all.sort((a1, a2) -> Integer.compare(a2.getLikes(), a1.getLikes()));

        long total = all.size();
        int fromIdx = (page - 1) * pageSize;
        int toIdx = (int) Math.min(fromIdx + pageSize, total);
        List<com.campuslink.dto.LikedItemVO> pageData =
                fromIdx >= total ? new ArrayList<>() : all.subList(fromIdx, toIdx);
        long totalPages = (total + pageSize - 1) / pageSize;

        return new PageResult<>(pageData, total, (long) page, (long) pageSize, totalPages);
    }

    /**
     * 计算连续签到天数
     */
    private int calculateConsecutiveDays(Long userId) {
        // 查询最近30天的签到记录
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        LambdaQueryWrapper<PointsLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsLog::getUserId, userId);
        wrapper.eq(PointsLog::getReason, "每日签到");
        wrapper.ge(PointsLog::getCreatedAt, thirtyDaysAgo);
        wrapper.orderByDesc(PointsLog::getCreatedAt);

        List<PointsLog> logs = pointsLogMapper.selectList(wrapper);

        if (logs.isEmpty()) {
            return 1; // 今天是第一天
        }

        // 计算连续天数
        int consecutiveDays = 1;
        LocalDateTime checkDate = LocalDateTime.now().minusDays(1);

        for (PointsLog log : logs) {
            LocalDateTime logDate = log.getCreatedAt();
            LocalDateTime checkDateStart = checkDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime checkDateEnd = checkDate.withHour(23).withMinute(59).withSecond(59).withNano(999999999);

            // 如果当天有签到记录
            if (logDate.isAfter(checkDateStart) && logDate.isBefore(checkDateEnd)) {
                consecutiveDays++;
                checkDate = checkDate.minusDays(1);
            } else {
                // 中断了连续签到
                break;
            }
        }

        return consecutiveDays;
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证旧密码
        if (!encryptPassword(request.getOldPassword()).equals(user.getPasswordHash())) {
            throw new BusinessException(400, "旧密码不正确");
        }

        // 检查新密码不能与旧密码相同
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BusinessException(400, "新密码不能与旧密码相同");
        }

        // 更新密码
        user.setPasswordHash(encryptPassword(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户修改密码成功: userId={}", userId);
    }

    /**
     * 获取用户贡献排行榜
     */
    public PageResult<UserVO> getUserRanking(Integer page, Integer pageSize) {
        // 构建分页对象
        Page<User> pageParam = new Page<>(page, pageSize);

        // 构建查询条件：按积分降序排序，只查询正常状态的用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getStatus, 1)  // 只查询正常用户
                .orderByDesc(User::getPoints)  // 按积分降序
                .orderByDesc(User::getCreatedAt);  // 积分相同时按注册时间排序

        // 执行查询
        Page<User> userPage = userMapper.selectPage(pageParam, queryWrapper);

        // 转换为 VO 列表
        List<UserVO> userVOList = new ArrayList<>();
        for (User user : userPage.getRecords()) {
            userVOList.add(convertToVO(user));
        }

        // 构建分页结果
        return new PageResult<>(
                userVOList,
                userPage.getTotal(),
                (long) page,
                (long) pageSize,
                userPage.getPages()
        );
    }

    /**
     * 转换为 VO 对象
     */
    private UserVO convertToVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        // 手机号脱敏
        if (user.getPhone() != null && user.getPhone().length() == 11) {
            userVO.setPhone(user.getPhone().substring(0, 3) + "****" + user.getPhone().substring(7));
        }

        // 查询学校名称
        if (user.getSchoolId() != null) {
            School school = schoolMapper.selectById(user.getSchoolId());
            if (school != null) {
                userVO.setSchoolName(school.getSchoolName());
            }
        }

        return userVO;
    }

    /**
     * 根据微信 OpenID 查询用户
     */
    public User findByWechatOpenid(String openid) {
        return userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getWechatOpenid, openid)
        );
    }

    /**
     * 根据微信 UnionID 查询用户
     */
    public User findByWechatUnionid(String unionid) {
        if (unionid == null || unionid.isEmpty()) {
            return null;
        }
        return userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getWechatUnionid, unionid)
        );
    }

    /**
     * 创建微信小程序用户（自动注册）
     */
    @Transactional
    public User createWechatUser(String openid, String unionid, String nickname, String avatarUrl) {
        User user = new User();
        user.setWechatOpenid(openid);
        user.setWechatUnionid(unionid);

        // 设置昵称（如果未提供则使用默认值）
        if (nickname != null && !nickname.isEmpty()) {
            user.setNickname(nickname);
        } else {
            user.setNickname("微信用户_" + generateRandomString(6));
        }

        // 设置用户名（使用 wx_ + openid前8位）
        user.setUsername("wx_" + openid.substring(0, Math.min(8, openid.length())));

        // 设置头像
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            user.setAvatarUrl(avatarUrl);
        }

        // 设置默认值
        user.setRole("student");
        user.setPoints(100);  // 注册赠送 100 积分
        user.setLevel(1);
        user.setStatus(1);  // 正常状态
        user.setIsVerified(0);  // 未实名认证
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setLastLoginTime(LocalDateTime.now());

        // 保存用户
        userMapper.insert(user);

        // 记录积分日志（注册赠送）
        addPointsLog(user.getUId(), 100, "register", "注册奖励");

        log.info("微信用户自动注册成功: userId={}, openid={}, username={}",
                user.getUId(), openid, user.getUsername());

        return user;
    }

    /**
     * 记录积分变化日志（私有方法）
     */
    private void addPointsLog(Long userId, Integer points, String relatedType, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }

        PointsLog pointsLog = new PointsLog();
        pointsLog.setUserId(userId);
        pointsLog.setPointsChange(points);
        pointsLog.setPointsAfter(user.getPoints());
        pointsLog.setReason(reason);
        pointsLog.setRelatedType(relatedType);
        pointsLog.setRelatedId(userId);
        pointsLog.setCreatedAt(LocalDateTime.now());
        pointsLogMapper.insert(pointsLog);
    }

    /**
     * 生成随机字符串
     */
    private String generateRandomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
