package com.campuslink.controller;

import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.dto.resource.*;
import com.campuslink.dto.download.DownloadLogResponse;
import com.campuslink.dto.comment.AddCommentRequest;
import com.campuslink.dto.comment.ResourceCommentResponse;
import com.campuslink.service.ResourceService;
import com.campuslink.service.DownloadLogService;
import com.campuslink.service.ResourceCommentService;
import com.campuslink.common.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 资源控制器
 */
@Tag(name = "资源模块", description = "资源上传、下载、列表、点赞等接口")
@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;
    private final DownloadLogService downloadLogService;
    private final ResourceCommentService commentService;
    private final com.campuslink.service.OssService ossService;

    @Operation(summary = "获取 OSS 上传签名", description = "客户端直传 OSS 前需要先获取签名")
    @GetMapping("/upload/signature")
    public Result<com.campuslink.dto.resource.OssSignatureResponse> getUploadSignature(
            @Parameter(description = "文件名") @RequestParam String fileName
    ) {
        com.campuslink.dto.resource.OssSignatureResponse signature = ossService.generateUploadSignature(fileName);
        return Result.success(signature);
    }

    @Operation(summary = "上传资源")
    @PostMapping("/upload")
    public Result<Map<String, Long>> uploadResource(
            @Valid @RequestBody UploadResourceRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Long resourceId = resourceService.uploadResource(userId, request);
        return Result.success("上传成功，等待审核", Map.of("resourceId", resourceId));
    }

    @Operation(summary = "获取资源列表")
    @GetMapping("/list")
    public Result<PageResult<ResourceListResponse>> getResourceList(
            @Parameter(description = "资源分类") @RequestParam(required = false) String category,
            @Parameter(description = "学校ID") @RequestParam(required = false) Long schoolId,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "created_at") String sortBy,
            @Parameter(description = "排序方式") @RequestParam(defaultValue = "desc") String sortOrder,
            HttpServletRequest httpRequest
    ) {
        // 获取当前用户ID（可能为null，表示未登录）
        Long currentUserId = (Long) httpRequest.getAttribute("userId");

        PageResult<ResourceListResponse> result = resourceService.getResourceList(
                category, schoolId, keyword, page, pageSize, sortBy, sortOrder, currentUserId
        );
        return Result.success(result);
    }

    @Operation(summary = "获取资源详情")
    @GetMapping("/{id}")
    public Result<ResourceResponse> getResourceById(
            @Parameter(description = "资源ID") @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        ResourceResponse resource = resourceService.getResourceById(id, userId);
        return Result.success(resource);
    }

    @Operation(summary = "下载资源")
    @PostMapping("/{id}/download")
    public Result<DownloadResourceResponse> downloadResource(
            @Parameter(description = "资源ID") @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        DownloadResourceResponse response = resourceService.downloadResource(id, userId);
        return Result.success("下载成功", response);
    }

    @Operation(summary = "点赞资源")
    @PostMapping("/{id}/like")
    public Result<Map<String, Integer>> likeResource(
            @Parameter(description = "资源ID") @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Integer likes = resourceService.likeResource(id, userId);
        return Result.success("点赞成功", Map.of("likes", likes));
    }

    @Operation(summary = "取消点赞")
    @DeleteMapping("/{id}/like")
    public Result<Map<String, Integer>> unlikeResource(
            @Parameter(description = "资源ID") @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Integer likes = resourceService.unlikeResource(id, userId);
        return Result.success("取消点赞成功", Map.of("likes", likes));
    }

    @Operation(summary = "搜索资源")
    @GetMapping("/search")
    public Result<PageResult<ResourceListResponse>> searchResources(
            @Parameter(description = "搜索关键词") @RequestParam String q,
            @Parameter(description = "资源分类") @RequestParam(required = false) String category,
            @Parameter(description = "学校ID") @RequestParam(required = false) Long schoolId,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest httpRequest
    ) {
        // 获取当前用户ID（可能为null，表示未登录）
        Long currentUserId = (Long) httpRequest.getAttribute("userId");

        PageResult<ResourceListResponse> result = resourceService.getResourceList(
                category, schoolId, q, page, pageSize, "created_at", "desc", currentUserId
        );
        return Result.success(result);
    }

    @Operation(summary = "获取待审核资源列表", description = "管理员获取所有待审核的资源")
    @GetMapping("/pending")
    public Result<PageResult<ResourceListResponse>> getPendingResources(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestAttribute("role") String role
    ) {
        // 验证管理员权限
        if (!"teacher".equals(role)) {
            return Result.error(com.campuslink.common.ResultCode.PERMISSION_DENIED);
        }

        PageResult<ResourceListResponse> result = resourceService.getPendingResources(page, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "审核通过资源", description = "管理员审核通过资源")
    @PutMapping("/{id}/approve")
    public Result<Void> approveResource(
            @Parameter(description = "资源ID") @PathVariable Long id,
            @RequestAttribute("role") String role
    ) {
        // 验证管理员权限
        if (!"teacher".equals(role)) {
            return Result.error(com.campuslink.common.ResultCode.PERMISSION_DENIED);
        }

        resourceService.approveResource(id);
        return Result.success("审核通过");
    }

    @Operation(summary = "拒绝资源", description = "管理员拒绝资源并说明原因")
    @PutMapping("/{id}/reject")
    public Result<Void> rejectResource(
            @Parameter(description = "资源ID") @PathVariable Long id,
            @Valid @RequestBody ReviewResourceRequest request,
            @RequestAttribute("role") String role
    ) {
        // 验证管理员权限
        if (!"teacher".equals(role)) {
            return Result.error(com.campuslink.common.ResultCode.PERMISSION_DENIED);
        }

        resourceService.rejectResource(id, request.getRejectReason());
        return Result.success("已拒绝资源");
    }

    @Operation(summary = "获取我上传的资源列表")
    @GetMapping("/my")
    public Result<PageResult<ResourceListResponse>> getMyResources(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        PageResult<ResourceListResponse> result = resourceService.getMyResources(userId, page, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "获取我的下载历史")
    @GetMapping("/download-history")
    public Result<PageResult<DownloadLogResponse>> getMyDownloadHistory(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        PageResult<DownloadLogResponse> result = downloadLogService.getMyDownloadHistory(userId, page, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "获取资源的下载记录", description = "管理员查看资源的所有下载记录")
    @GetMapping("/{id}/download-logs")
    public Result<PageResult<DownloadLogResponse>> getResourceDownloadHistory(
            @Parameter(description = "资源ID") @PathVariable Long id,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestAttribute("role") String role
    ) {
        // 验证管理员权限
        if (!"teacher".equals(role) && !"admin".equals(role)) {
            return Result.error(ResultCode.PERMISSION_DENIED);
        }

        PageResult<DownloadLogResponse> result = downloadLogService.getResourceDownloadHistory(id, page, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "添加评论", description = "对资源发表评论或回复")
    @PostMapping("/{id}/comments")
    public Result<Map<String, Long>> addComment(
            @Parameter(description = "资源ID") @PathVariable Long id,
            @Valid @RequestBody AddCommentRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Long commentId = commentService.addComment(id, userId, request);
        return Result.success("评论成功", Map.of("commentId", commentId));
    }

    @Operation(summary = "获取评论列表", description = "获取资源的所有评论（分页）")
    @GetMapping("/{id}/comments")
    public Result<PageResult<ResourceCommentResponse>> getComments(
            @Parameter(description = "资源ID") @PathVariable Long id,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        PageResult<ResourceCommentResponse> result = commentService.getResourceComments(id, page, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "删除评论", description = "删除自己的评论")
    @DeleteMapping("/comments/{commentId}")
    public Result<Void> deleteComment(
            @Parameter(description = "评论ID") @PathVariable Long commentId,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        commentService.deleteComment(commentId, userId);
        return Result.success("删除成功");
    }

    @Operation(summary = "评分资源", description = "用户对资源进行评分（1-5星），rating=0表示取消评分")
    @PostMapping("/{id}/rate")
    public Result<Map<String, Object>> rateResource(
            @Parameter(description = "资源ID") @PathVariable Long id,
            @Parameter(description = "评分（0-5，0表示取消评分）") @RequestBody Map<String, Integer> request,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Integer rating = request.get("rating");

        if (rating == null) {
            return Result.error(400, "评分不能为空");
        }

        return resourceService.rateResource(id, userId, rating);
    }
}
