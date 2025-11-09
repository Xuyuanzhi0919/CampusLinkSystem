package com.campuslink.controller;

import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.dto.report.CreateReportRequest;
import com.campuslink.dto.report.HandleReportRequest;
import com.campuslink.dto.report.ReportResponse;
import com.campuslink.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 举报管理接口
 */
@Tag(name = "举报管理")
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 创建举报
     */
    @Operation(summary = "创建举报")
    @PostMapping
    public Result<Long> createReport(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateReportRequest request
    ) {
        Long reportId = reportService.createReport(userId, request);
        return Result.success("举报成功", reportId);
    }

    /**
     * 获取举报列表(管理员)
     */
    @Operation(summary = "获取举报列表")
    @GetMapping("/list")
    public Result<PageResult<ReportResponse>> getReportList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer reportType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        PageResult<ReportResponse> result = reportService.getReportList(status, reportType, page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取我的举报列表
     */
    @Operation(summary = "获取我的举报列表")
    @GetMapping("/my")
    public Result<PageResult<ReportResponse>> getMyReports(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        PageResult<ReportResponse> result = reportService.getMyReports(userId, page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取举报详情
     */
    @Operation(summary = "获取举报详情")
    @GetMapping("/{reportId}")
    public Result<ReportResponse> getReportDetail(
            @PathVariable Long reportId
    ) {
        ReportResponse report = reportService.getReportDetail(reportId);
        return Result.success(report);
    }

    /**
     * 处理举报(管理员)
     */
    @Operation(summary = "处理举报")
    @PutMapping("/{reportId}/handle")
    public Result<Void> handleReport(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long reportId,
            @Valid @RequestBody HandleReportRequest request
    ) {
        reportService.handleReport(userId, reportId, request);
        return Result.success("处理成功");
    }

    /**
     * 删除举报(撤销)
     */
    @Operation(summary = "删除举报")
    @DeleteMapping("/{reportId}")
    public Result<Void> deleteReport(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long reportId
    ) {
        reportService.deleteReport(userId, reportId);
        return Result.success("撤销成功");
    }

    /**
     * 获取待处理举报数量(管理员)
     */
    @Operation(summary = "获取待处理举报数量")
    @GetMapping("/pending-count")
    public Result<Integer> getPendingCount() {
        Integer count = reportService.getPendingCount();
        return Result.success(count);
    }
}
