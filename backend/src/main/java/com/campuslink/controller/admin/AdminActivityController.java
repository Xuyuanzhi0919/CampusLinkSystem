package com.campuslink.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.entity.Activity;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.ActivityMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员活动管理接口
 */
@Tag(name = "管理端 - 活动管理")
@RestController
@RequestMapping("/admin/activities")
@RequiredArgsConstructor
public class AdminActivityController {

    private final ActivityMapper activityMapper;

    @Operation(summary = "活动列表", description = "status: 0-待开始 1-进行中 2-已结束 3-已取消")
    @GetMapping
    public Result<PageResult<Activity>> listActivities(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Page<Activity> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Activity::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(Activity::getStatus, status);
        }
        wrapper.orderByDesc(Activity::getCreatedAt);

        Page<Activity> result = activityMapper.selectPage(p, wrapper);
        return Result.success(new PageResult<>(
                result.getRecords(), result.getTotal(),
                (long) page, (long) pageSize, result.getPages()
        ));
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
