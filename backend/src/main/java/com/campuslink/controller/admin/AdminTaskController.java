package com.campuslink.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.entity.Task;
import com.campuslink.entity.User;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.TaskMapper;
import com.campuslink.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理员任务管理接口
 */
@Tag(name = "管理端 - 任务管理")
@RestController
@RequestMapping("/admin/tasks")
@RequiredArgsConstructor
public class AdminTaskController {

    private final TaskMapper taskMapper;
    private final UserMapper userMapper;

    @Operation(summary = "任务列表", description = "status: 0待接单 2进行中 3待确认 4已完成 5已取消 6已超时")
    @GetMapping
    public Result<PageResult<Task>> listTasks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Page<Task> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Task::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(Task::getCreatedAt, LocalDate.parse(startDate).atStartOfDay());
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(Task::getCreatedAt, LocalDate.parse(endDate).atTime(23, 59, 59));
        }
        wrapper.orderByDesc(Task::getCreatedAt);

        Page<Task> result = taskMapper.selectPage(p, wrapper);
        List<Task> records = result.getRecords();

        // 批量解析发布者/接单者昵称
        Set<Long> uids = new HashSet<>();
        records.forEach(t -> {
            uids.add(t.getPublisherId());
            if (t.getAccepterId() != null) uids.add(t.getAccepterId());
        });
        if (!uids.isEmpty()) {
            Map<Long, String> nameMap = userMapper.selectList(
                    new LambdaQueryWrapper<User>().in(User::getUId, uids)
            ).stream().collect(Collectors.toMap(
                    User::getUId,
                    u -> StringUtils.hasText(u.getNickname()) ? u.getNickname() : u.getUsername()
            ));
            records.forEach(t -> {
                t.setPublisherName(nameMap.getOrDefault(t.getPublisherId(), "uid=" + t.getPublisherId()));
                if (t.getAccepterId() != null) {
                    t.setAccepterName(nameMap.getOrDefault(t.getAccepterId(), "uid=" + t.getAccepterId()));
                }
            });
        }

        return Result.success(new PageResult<>(
                records, result.getTotal(),
                (long) page, (long) pageSize, result.getPages()
        ));
    }

    @Operation(summary = "强制取消任务", description = "将任务状态设为 5（已取消）")
    @PutMapping("/{taskId}/cancel")
    public Result<Void> cancelTask(@PathVariable Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) throw new BusinessException(404, "任务不存在");
        if (task.getStatus() == 4 || task.getStatus() == 5 || task.getStatus() == 6) {
            throw new BusinessException(400, "任务已结束，无法取消");
        }
        task.setStatus(5);
        taskMapper.updateById(task);
        return Result.success("任务已强制取消");
    }
}
