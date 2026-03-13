package com.campuslink.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.entity.AdminOperationLog;
import com.campuslink.mapper.AdminOperationLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员操作日志接口
 */
@Tag(name = "管理端 - 操作日志")
@RestController
@RequestMapping("/admin/audit")
@RequiredArgsConstructor
public class AdminAuditController {

    private final AdminOperationLogMapper auditLogMapper;

    @Operation(summary = "操作日志列表", description = "支持操作类型/操作人筛选，按时间倒序")
    @GetMapping("/logs")
    public Result<PageResult<AdminOperationLog>> listLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Page<AdminOperationLog> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<AdminOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(action)) wrapper.like(AdminOperationLog::getAction, action);
        if (operatorId != null)          wrapper.eq(AdminOperationLog::getOperatorId, operatorId);
        wrapper.orderByDesc(AdminOperationLog::getCreatedAt);

        Page<AdminOperationLog> result = auditLogMapper.selectPage(p, wrapper);
        return Result.success(new PageResult<>(
                result.getRecords(), result.getTotal(),
                (long) page, (long) pageSize, result.getPages()
        ));
    }
}
