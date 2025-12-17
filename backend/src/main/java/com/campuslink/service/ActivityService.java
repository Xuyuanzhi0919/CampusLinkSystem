package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.activity.ActivityParticipantResponse;
import com.campuslink.dto.activity.ActivityResponse;
import com.campuslink.dto.activity.CreateActivityRequest;
import com.campuslink.entity.*;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 活动服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityMapper activityMapper;
    private final ActivityParticipantMapper participantMapper;
    private final ClubMapper clubMapper;
    private final ClubMemberMapper clubMemberMapper;
    private final UserMapper userMapper;
    private final PointsLogMapper pointsLogMapper;
    private final FavoriteService favoriteService;

    /**
     * 创建活动
     */
    @Transactional
    public Long createActivity(Long userId, Long clubId, CreateActivityRequest request) {
        // 验证社团存在
        Club club = clubMapper.selectById(clubId);
        if (club == null) {
            throw new BusinessException(ResultCode.CLUB_NOT_FOUND);
        }

        // 验证用户是否是社团管理员或创始人
        LambdaQueryWrapper<ClubMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubMember::getClubId, clubId)
                .eq(ClubMember::getUserId, userId);
        ClubMember member = clubMemberMapper.selectOne(wrapper);

        if (member == null || "member".equals(member.getRole())) {
            throw new BusinessException(ResultCode.NOT_CLUB_ADMIN);
        }

        // 验证时间
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "结束时间不能早于开始时间");
        }

        // 创建活动
        Activity activity = new Activity();
        BeanUtils.copyProperties(request, activity);
        activity.setClubId(clubId);
        activity.setCurrentParticipants(0);
        activity.setStatus(0); // 未开始
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());

        activityMapper.insert(activity);

        log.info("用户 {} 为社团 {} 创建了活动 {}", userId, clubId, activity.getActivityId());
        return activity.getActivityId();
    }

    /**
     * 获取活动列表
     */
    public PageResult<ActivityResponse> getActivityList(Long userId, Integer page, Integer pageSize,
                                                         Long clubId, Integer status, String sortBy, String keyword, String activityType) {
        Page<Activity> activityPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();

        if (clubId != null) {
            wrapper.eq(Activity::getClubId, clubId);
        }

        // 🎯 如果指定了状态，使用动态状态判断（基于时间）
        LocalDateTime now = LocalDateTime.now();
        if (status != null) {
            if (status == 0) {
                // 未开始：开始时间在未来
                wrapper.gt(Activity::getStartTime, now);
            } else if (status == 1) {
                // 进行中：当前时间在开始和结束之间
                wrapper.le(Activity::getStartTime, now)
                       .gt(Activity::getEndTime, now);
            } else if (status == 2) {
                // 已结束：结束时间在过去
                wrapper.le(Activity::getEndTime, now);
            }
        }

        // 🎯 活动类型筛选 (新增)
        if (activityType != null && !activityType.trim().isEmpty() && !"all".equals(activityType)) {
            wrapper.eq(Activity::getActivityType, activityType);
        }

        // 🎯 关键字搜索 - 搜索标题和地点
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Activity::getTitle, keyword)
                    .or()
                    .like(Activity::getLocation, keyword));
        }

        // 🎯 排序逻辑
        if ("hot".equals(sortBy)) {
            // 按热度排序（报名人数降序）
            wrapper.orderByDesc(Activity::getCurrentParticipants);
        } else if ("time".equals(sortBy)) {
            // 按时间排序（开始时间升序）
            wrapper.orderByAsc(Activity::getStartTime);
        } else {
            // 默认按创建时间降序（最新）
            wrapper.orderByDesc(Activity::getCreatedAt);
        }

        activityPage = activityMapper.selectPage(activityPage, wrapper);

        List<ActivityResponse> activityResponses = activityPage.getRecords().stream()
                .map(activity -> convertToResponse(activity, userId))
                .collect(Collectors.toList());

        return new PageResult<>(
                activityResponses,
                activityPage.getTotal(),
                activityPage.getCurrent(),
                activityPage.getSize(),
                activityPage.getPages()
        );
    }

    /**
     * 获取活动详情
     */
    public ActivityResponse getActivityDetail(Long activityId, Long userId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }

        return convertToResponse(activity, userId);
    }

    /**
     * 报名活动
     */
    @Transactional
    public void joinActivity(Long activityId, Long userId) {
        // 验证活动存在
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }

        // 🎯 检查活动状态 - 只有未开始(0)和进行中(1)可以报名
        if (activity.getStatus() == 2) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "活动已结束，无法报名");
        }
        if (activity.getStatus() == 3) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "活动已取消，无法报名");
        }

        // 检查是否已报名
        LambdaQueryWrapper<ActivityParticipant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityParticipant::getActivityId, activityId)
                .eq(ActivityParticipant::getUserId, userId);
        if (participantMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.ALREADY_REGISTERED);
        }

        // 检查是否已满员
        if (activity.getMaxParticipants() != null &&
            activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            throw new BusinessException(ResultCode.ACTIVITY_FULL);
        }

        // 添加参与记录
        ActivityParticipant participant = new ActivityParticipant();
        participant.setActivityId(activityId);
        participant.setUserId(userId);
        participant.setIsSignedIn(0);
        participant.setJoinedAt(LocalDateTime.now());
        participantMapper.insert(participant);

        // 更新参与人数
        activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
        activityMapper.updateById(activity);

        log.info("用户 {} 报名了活动 {}", userId, activityId);
    }

    /**
     * 取消报名
     */
    @Transactional
    public void cancelJoin(Long activityId, Long userId) {
        // 验证活动存在
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }

        // 检查是否已报名
        LambdaQueryWrapper<ActivityParticipant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityParticipant::getActivityId, activityId)
                .eq(ActivityParticipant::getUserId, userId);
        ActivityParticipant participant = participantMapper.selectOne(wrapper);

        if (participant == null) {
            throw new BusinessException(ResultCode.NOT_ACTIVITY_PARTICIPANT);
        }

        // 检查活动状态
        if (activity.getStatus() != 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "活动已开始或已结束，无法取消报名");
        }

        // 删除参与记录
        participantMapper.deleteById(participant.getApId());

        // 更新参与人数
        activity.setCurrentParticipants(activity.getCurrentParticipants() - 1);
        activityMapper.updateById(activity);

        log.info("用户 {} 取消了活动 {} 的报名", userId, activityId);
    }

    /**
     * 签到
     */
    @Transactional
    public void signIn(Long activityId, Long userId) {
        // 验证活动存在
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }

        // 检查是否已报名
        LambdaQueryWrapper<ActivityParticipant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityParticipant::getActivityId, activityId)
                .eq(ActivityParticipant::getUserId, userId);
        ActivityParticipant participant = participantMapper.selectOne(wrapper);

        if (participant == null) {
            throw new BusinessException(ResultCode.NOT_ACTIVITY_PARTICIPANT);
        }

        // 检查是否已签到
        if (participant.getIsSignedIn() == 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已签到，请勿重复签到");
        }

        // 🎯 优化：检查活动时间（基于实际时间而非状态字段）
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "活动尚未开始，无法签到");
        }
        if (now.isAfter(activity.getEndTime())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "活动已结束，无法签到");
        }

        // 更新签到状态
        participant.setIsSignedIn(1);
        participant.setSignedInAt(LocalDateTime.now());
        participantMapper.updateById(participant);

        // 如果有积分奖励，发放积分
        if (activity.getRewardPoints() > 0) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setPoints(user.getPoints() + activity.getRewardPoints());
                userMapper.updateById(user);

                // 记录积分日志
                recordPointsLog(userId, activity.getRewardPoints(), user.getPoints(),
                        "参与活动签到：" + activity.getTitle(), "activity", activityId);
            }
        }

        log.info("用户 {} 签到了活动 {}", userId, activityId);
    }

    /**
     * 获取活动参与者列表
     */
    public PageResult<ActivityParticipantResponse> getParticipants(Long activityId, Integer page, Integer pageSize) {
        // 验证活动存在
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }

        Page<ActivityParticipant> participantPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<ActivityParticipant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityParticipant::getActivityId, activityId)
                .orderByDesc(ActivityParticipant::getJoinedAt);

        participantPage = participantMapper.selectPage(participantPage, wrapper);

        List<ActivityParticipantResponse> participantResponses = participantPage.getRecords().stream()
                .map(this::convertToParticipantResponse)
                .collect(Collectors.toList());

        return new PageResult<>(
                participantResponses,
                participantPage.getTotal(),
                participantPage.getCurrent(),
                participantPage.getSize(),
                participantPage.getPages()
        );
    }

    /**
     * 获取我报名的活动
     */
    public PageResult<ActivityResponse> getMyActivities(Long userId, Integer page, Integer pageSize) {
        // 查询用户报名的活动ID列表
        LambdaQueryWrapper<ActivityParticipant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityParticipant::getUserId, userId);
        List<ActivityParticipant> participantList = participantMapper.selectList(wrapper);

        if (participantList.isEmpty()) {
            return new PageResult<>(new ArrayList<>(), 0L, (long) page, (long) pageSize, 0L);
        }

        List<Long> activityIds = participantList.stream()
                .map(ActivityParticipant::getActivityId)
                .collect(Collectors.toList());

        // 分页查询活动
        Page<Activity> activityPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Activity> activityWrapper = new LambdaQueryWrapper<>();
        activityWrapper.in(Activity::getActivityId, activityIds)
                .orderByDesc(Activity::getCreatedAt);

        activityPage = activityMapper.selectPage(activityPage, activityWrapper);

        List<ActivityResponse> activityResponses = activityPage.getRecords().stream()
                .map(activity -> convertToResponse(activity, userId))
                .collect(Collectors.toList());

        return new PageResult<>(
                activityResponses,
                activityPage.getTotal(),
                activityPage.getCurrent(),
                activityPage.getSize(),
                activityPage.getPages()
        );
    }

    /**
     * 转换为活动响应对象
     */
    private ActivityResponse convertToResponse(Activity activity, Long userId) {
        ActivityResponse response = new ActivityResponse();
        BeanUtils.copyProperties(activity, response);

        // 查询社团名称（仅社团活动有值）
        if (activity.getClubId() != null) {
            Club club = clubMapper.selectById(activity.getClubId());
            if (club != null) {
                response.setClubName(club.getClubName());
            }
        }

        // 🎯 动态计算活动状态（基于时间）
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            response.setStatus(0); // 未开始
        } else if (now.isAfter(activity.getEndTime())) {
            response.setStatus(2); // 已结束
        } else {
            response.setStatus(1); // 进行中
        }

        // 🎯 计算剩余名额
        Integer maxParticipants = activity.getMaxParticipants() != null ? activity.getMaxParticipants() : 0;
        Integer currentParticipants = activity.getCurrentParticipants() != null ? activity.getCurrentParticipants() : 0;
        response.setRemainingSlots(Math.max(0, maxParticipants - currentParticipants));

        // 查询当前用户是否已报名和签到
        if (userId != null) {
            LambdaQueryWrapper<ActivityParticipant> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ActivityParticipant::getActivityId, activity.getActivityId())
                    .eq(ActivityParticipant::getUserId, userId);
            ActivityParticipant participant = participantMapper.selectOne(wrapper);

            if (participant != null) {
                response.setIsJoined(true);
                response.setIsSignedIn(participant.getIsSignedIn() == 1);
            } else {
                response.setIsJoined(false);
                response.setIsSignedIn(false);
            }

            // 🎯 查询是否已收藏
            response.setIsFavorited(favoriteService.isFavorited(userId, "activity", activity.getActivityId()));
        } else {
            // 🎯 游客模式：设置默认值
            response.setIsJoined(false);
            response.setIsSignedIn(false);
            response.setIsFavorited(false);
        }

        return response;
    }

    /**
     * 转换为参与者响应对象
     */
    private ActivityParticipantResponse convertToParticipantResponse(ActivityParticipant participant) {
        ActivityParticipantResponse response = new ActivityParticipantResponse();
        response.setUserId(participant.getUserId());
        response.setIsSignedIn(participant.getIsSignedIn() == 1);
        response.setSignedInAt(participant.getSignedInAt());
        response.setJoinedAt(participant.getJoinedAt());

        // 查询用户信息
        User user = userMapper.selectById(participant.getUserId());
        if (user != null) {
            response.setNickname(user.getNickname());
            response.setAvatarUrl(user.getAvatarUrl());
        }

        return response;
    }

    /**
     * 记录积分日志
     */
    private void recordPointsLog(Long userId, Integer pointsChange, Integer pointsAfter,
                                  String reason, String relatedType, Long relatedId) {
        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setPointsChange(pointsChange);
        log.setPointsAfter(pointsAfter);
        log.setReason(reason);
        log.setRelatedType(relatedType);
        log.setRelatedId(relatedId);
        log.setCreatedAt(LocalDateTime.now());
        pointsLogMapper.insert(log);
    }
}
