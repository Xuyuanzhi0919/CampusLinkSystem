package com.campuslink.controller.admin;

import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.dto.report.HandleReportRequest;
import com.campuslink.dto.report.ReportResponse;
import com.campuslink.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员举报处理接口
 */
@Tag(name = "管理端 - 举报管理")
@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    @Operation(summary = "举报列表", description = "status: 0-待处理 1-已处理 2-已驳回")
    @GetMapping
    public Result<PageResult<ReportResponse>> listReports(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer reportType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(reportService.getReportList(status, reportType, page, pageSize));
    }

    @Operation(summary = "举报详情")
    @GetMapping("/{reportId}")
    public Result<ReportResponse> getReport(@PathVariable Long reportId) {
        return Result.success(reportService.getReportDetail(reportId));
    }

    @Operation(summary = "处理举报")
    @PutMapping("/{reportId}/handle")
    public Result<Void> handleReport(
            @Parameter(hidden = true) @RequestAttribute("userId") Long operatorId,
            @PathVariable Long reportId,
            @Valid @RequestBody HandleReportRequest req) {
        reportService.handleReport(operatorId, reportId, req);
        return Result.success("处理成功");
    }

    @Operation(summary = "待处理举报数量")
    @GetMapping("/pending-count")
    public Result<Integer> pendingCount() {
        return Result.success(reportService.getPendingCount());
    }
}
