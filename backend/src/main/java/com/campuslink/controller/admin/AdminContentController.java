package com.campuslink.controller.admin;

import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.dto.admin.AdminContentQueryRequest;
import com.campuslink.dto.admin.UpdateContentStatusRequest;
import com.campuslink.entity.Question;
import com.campuslink.entity.Resource;
import com.campuslink.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
            @PathVariable Long resourceId,
            @Valid @RequestBody UpdateContentStatusRequest req) {
        adminService.updateResourceStatus(resourceId, req);
        return Result.success("操作成功");
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
}
