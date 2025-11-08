package com.campuslink.controller;

import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.dto.resource.*;
import com.campuslink.service.ResourceService;
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
            @Parameter(description = "排序方式") @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        PageResult<ResourceListResponse> result = resourceService.getResourceList(
                category, schoolId, keyword, page, pageSize, sortBy, sortOrder
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
            @Parameter(description = "资源ID") @PathVariable Long id
    ) {
        Integer likes = resourceService.likeResource(id);
        return Result.success("点赞成功", Map.of("likes", likes));
    }

    @Operation(summary = "取消点赞")
    @DeleteMapping("/{id}/like")
    public Result<Map<String, Integer>> unlikeResource(
            @Parameter(description = "资源ID") @PathVariable Long id
    ) {
        Integer likes = resourceService.unlikeResource(id);
        return Result.success("取消点赞成功", Map.of("likes", likes));
    }

    @Operation(summary = "搜索资源")
    @GetMapping("/search")
    public Result<PageResult<ResourceListResponse>> searchResources(
            @Parameter(description = "搜索关键词") @RequestParam String q,
            @Parameter(description = "资源分类") @RequestParam(required = false) String category,
            @Parameter(description = "学校ID") @RequestParam(required = false) Long schoolId,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        PageResult<ResourceListResponse> result = resourceService.getResourceList(
                category, schoolId, q, page, pageSize, "created_at", "desc"
        );
        return Result.success(result);
    }
}
