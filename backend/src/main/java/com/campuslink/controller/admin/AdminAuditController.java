package com.campuslink.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.entity.AdminOperationLog;
import com.campuslink.entity.User;
import com.campuslink.mapper.AdminOperationLogMapper;
import com.campuslink.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理员操作日志接口
 */
@Tag(name = "管理端 - 操作日志")
@RestController
@RequestMapping("/admin/audit")
@RequiredArgsConstructor
public class AdminAuditController {

    private final AdminOperationLogMapper auditLogMapper;
    private final UserMapper userMapper;

    @Operation(summary = "操作日志列表", description = "支持操作类型/操作人/日期范围筛选，按时间倒序")
    @GetMapping("/logs")
    public Result<PageResult<AdminOperationLog>> listLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Page<AdminOperationLog> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<AdminOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(action))    wrapper.eq(AdminOperationLog::getAction, action);
        if (operatorId != null)             wrapper.eq(AdminOperationLog::getOperatorId, operatorId);
        if (StringUtils.hasText(startDate)) wrapper.ge(AdminOperationLog::getCreatedAt,
                LocalDateTime.of(LocalDate.parse(startDate), LocalTime.MIN));
        if (StringUtils.hasText(endDate))   wrapper.le(AdminOperationLog::getCreatedAt,
                LocalDateTime.of(LocalDate.parse(endDate), LocalTime.MAX));
        wrapper.orderByDesc(AdminOperationLog::getCreatedAt);

        Page<AdminOperationLog> result = auditLogMapper.selectPage(p, wrapper);
        List<AdminOperationLog> records = result.getRecords();

        // 批量查询操作者昵称
        if (!records.isEmpty()) {
            Set<Long> ids = records.stream().map(AdminOperationLog::getOperatorId).collect(Collectors.toSet());
            Map<Long, String> nameMap = userMapper.selectList(
                    new LambdaQueryWrapper<User>().in(User::getUId, ids)
            ).stream().collect(Collectors.toMap(
                    User::getUId,
                    u -> u.getNickname() != null ? u.getNickname() : u.getUsername()
            ));
            records.forEach(r -> r.setOperatorName(nameMap.getOrDefault(r.getOperatorId(), "uid=" + r.getOperatorId())));
        }

        return Result.success(new PageResult<>(
                records, result.getTotal(),
                (long) page, (long) pageSize, result.getPages()
        ));
    }
}
