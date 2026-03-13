package com.campuslink.controller.admin;

import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.dto.admin.AdminContentQueryRequest;
import com.campuslink.dto.admin.UpdateContentStatusRequest;
import com.campuslink.entity.Answer;
import com.campuslink.entity.Question;
import com.campuslink.entity.Resource;
import com.campuslink.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员内容管理接口（资源审核、问答管理）
 */
@Tag(name = "管理端 - 内容管理")
@RestController
@RequestMapping("/admin/content")
@RequiredArgsConstructor
public class AdminContentController {

    private final AdminService adminService;

    // ==================== 资源管理 ====================

    @Operation(summary = "资源列表", description = "status: 0-待审核 1-已通过 2-已拒绝")
    @GetMapping("/resources")
    public Result<PageResult<Resource>> listResources(
            @ModelAttribute AdminContentQueryRequest req) {
        return Result.success(adminService.listResources(req));
    }

    @Operation(summary = "审核/下架资源", description = "status: 1-通过 2-拒绝")
    @PutMapping("/resources/{resourceId}/status")
    public Result<Void> updateResourceStatus(
            @Parameter(hidden = true) @RequestAttribute("userId") Long operatorId,
            @PathVariable Long resourceId,
            @Valid @RequestBody UpdateContentStatusRequest req) {
        adminService.updateResourceStatus(operatorId, resourceId, req);
        return Result.success("操作成功");
    }

    @Operation(summary = "批量审核资源", description = "status: 1-通过 2-拒绝；拒绝时可附 rejectReason")
    @PostMapping("/resources/batch-review")
    public Result<Map<String, Integer>> batchReviewResources(
            @Parameter(hidden = true) @RequestAttribute("userId") Long operatorId,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> ids = ((List<Number>) body.get("resourceIds")).stream().map(Number::longValue).toList();
        Integer status = (Integer) body.get("status");
        String reason = (String) body.getOrDefault("rejectReason", null);
        int count = adminService.batchReviewResources(operatorId, ids, status, reason);
        return Result.success(Map.of("count", count));
    }

    // ==================== 问答管理 ====================

    @Operation(summary = "问答列表", description = "status: 0-隐藏 1-正常")
    @GetMapping("/questions")
    public Result<PageResult<Question>> listQuestions(
            @ModelAttribute AdminContentQueryRequest req) {
        return Result.success(adminService.listQuestions(req));
    }

    @Operation(summary = "更新问答状态", description = "status: 0-隐藏 1-正常")
    @PutMapping("/questions/{questionId}/status")
    public Result<Void> updateQuestionStatus(
            @PathVariable Long questionId,
            @Valid @RequestBody UpdateContentStatusRequest req) {
        adminService.updateQuestionStatus(questionId, req);
        return Result.success("操作成功");
    }

    // ==================== 回答管理 ====================

    @Operation(summary = "回答列表", description = "status: 0-已隐藏 1-正常")
    @GetMapping("/answers")
    public Result<PageResult<Answer>> listAnswers(
            @ModelAttribute AdminContentQueryRequest req) {
        return Result.success(adminService.listAnswers(req));
    }

    @Operation(summary = "更新回答状态", description = "status: 0-隐藏 1-恢复")
    @PutMapping("/answers/{answerId}/status")
    public Result<Void> updateAnswerStatus(
            @PathVariable Long answerId,
            @Valid @RequestBody UpdateContentStatusRequest req) {
        adminService.updateAnswerStatus(answerId, req);
        return Result.success("操作成功");
    }
}
