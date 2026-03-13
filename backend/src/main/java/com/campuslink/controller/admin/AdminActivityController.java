package com.campuslink.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.dto.admin.AdminActivitySignupVO;
import com.campuslink.dto.admin.AdminUpdateActivityInfoRequest;
import com.campuslink.entity.Activity;
import com.campuslink.entity.ActivityParticipant;
import com.campuslink.entity.User;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.ActivityMapper;
import com.campuslink.mapper.ActivityParticipantMapper;
import com.campuslink.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理员活动管理接口
 */
@Tag(name = "管理端 - 活动管理")
@RestController
@RequestMapping("/admin/activities")
@RequiredArgsConstructor
public class AdminActivityController {

    private final ActivityMapper activityMapper;
    private final ActivityParticipantMapper participantMapper;
    private final UserMapper userMapper;

    @Operation(summary = "活动列表", description = "status: 0-待开始 1-进行中 2-已结束 3-已取消")
    @GetMapping
    public Result<PageResult<Activity>> listActivities(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Page<Activity> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) wrapper.like(Activity::getTitle, keyword);
        if (status != null) wrapper.eq(Activity::getStatus, status);
        if (StringUtils.hasText(startDate)) wrapper.ge(Activity::getCreatedAt, LocalDate.parse(startDate).atStartOfDay());
        if (StringUtils.hasText(endDate)) wrapper.le(Activity::getCreatedAt, LocalDate.parse(endDate).atTime(23, 59, 59));
        wrapper.orderByDesc(Activity::getCreatedAt);

        Page<Activity> result = activityMapper.selectPage(p, wrapper);
        return Result.success(new PageResult<>(
                result.getRecords(), result.getTotal(),
                (long) page, (long) pageSize, result.getPages()
        ));
    }

    @Operation(summary = "活动统计概览")
    @GetMapping("/stats")
    public Result<Map<String, Long>> getActivityStats() {
        long total = activityMapper.selectCount(new LambdaQueryWrapper<>());
        long ongoing = activityMapper.selectCount(new LambdaQueryWrapper<Activity>().eq(Activity::getStatus, 1));
        long pending = activityMapper.selectCount(new LambdaQueryWrapper<Activity>().eq(Activity::getStatus, 0));
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long thisMonth = activityMapper.selectCount(new LambdaQueryWrapper<Activity>().ge(Activity::getCreatedAt, monthStart));
        return Result.success(Map.of("total", total, "ongoing", ongoing, "pending", pending, "thisMonth", thisMonth));
    }

    @Operation(summary = "活动报名名单")
    @GetMapping("/{activityId}/signups")
    public Result<List<AdminActivitySignupVO>> getSignups(@PathVariable Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) throw new BusinessException(404, "活动不存在");

        List<ActivityParticipant> participants = participantMapper.selectList(
                new LambdaQueryWrapper<ActivityParticipant>()
                        .eq(ActivityParticipant::getActivityId, activityId)
                        .orderByAsc(ActivityParticipant::getJoinedAt)
        );
        if (participants.isEmpty()) return Result.success(List.of());

        Set<Long> userIds = participants.stream().map(ActivityParticipant::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userMapper.selectList(
                new LambdaQueryWrapper<User>().in(User::getUId, userIds)
        ).stream().collect(Collectors.toMap(User::getUId, u -> u));

        List<AdminActivitySignupVO> vos = participants.stream().map(p -> {
            AdminActivitySignupVO vo = new AdminActivitySignupVO();
            vo.setUserId(p.getUserId());
            vo.setIsSignedIn(p.getIsSignedIn());
            vo.setSignedInAt(p.getSignedInAt());
            vo.setJoinedAt(p.getJoinedAt());
            User u = userMap.get(p.getUserId());
            if (u != null) {
                vo.setUsername(u.getUsername());
                vo.setNickname(u.getNickname());
                vo.setAvatarUrl(u.getAvatarUrl());
            } else {
                vo.setUsername("uid=" + p.getUserId());
                vo.setNickname("-");
            }
            return vo;
        }).collect(Collectors.toList());

        return Result.success(vos);
    }

    @Operation(summary = "编辑活动信息（地点/人数/积分）")
    @PutMapping("/{activityId}/info")
    public Result<Void> updateInfo(@PathVariable Long activityId,
                                   @Valid @RequestBody AdminUpdateActivityInfoRequest req) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) throw new BusinessException(404, "活动不存在");
        if (activity.getStatus() == 3) throw new BusinessException(400, "已取消的活动无法修改");

        if (StringUtils.hasText(req.getLocation())) activity.setLocation(req.getLocation());
        if (req.getMaxParticipants() != null) activity.setMaxParticipants(req.getMaxParticipants());
        if (req.getRewardPoints() != null) activity.setRewardPoints(req.getRewardPoints());
        activityMapper.updateById(activity);
        return Result.success("活动信息已更新");
    }

    @Operation(summary = "强制取消活动")
    @PutMapping("/{activityId}/cancel")
    public Result<Void> cancelActivity(@PathVariable Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) throw new BusinessException(404, "活动不存在");
        if (activity.getStatus() == 2 || activity.getStatus() == 3) {
            throw new BusinessException(400, "活动已结束，无法取消");
        }
        activity.setStatus(3);
        activityMapper.updateById(activity);
        return Result.success("活动已取消");
    }
}
