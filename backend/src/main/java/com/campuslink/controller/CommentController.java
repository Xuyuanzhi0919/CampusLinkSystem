package com.campuslink.controller;

import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.dto.comment.CommentResponse;
import com.campuslink.dto.comment.CreateCommentRequest;
import com.campuslink.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评论控制器
 */
@Tag(name = "评论管理", description = "评论的增删查、点赞等接口")
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 创建评论
     */
    @Operation(summary = "创建评论", description = "对资源、问题、回答等发表评论或回复")
    @PostMapping
    public Result<Map<String, Long>> createComment(
            @Valid @RequestBody CreateCommentRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Long commentId = commentService.createComment(userId, request);
        return Result.success("评论成功", Map.of("commentId", commentId));
    }

    /**
     * 删除评论
     */
    @Operation(summary = "删除评论", description = "删除自己的评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(
            @Parameter(description = "评论ID") @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        commentService.deleteComment(id, userId);
        return Result.success("删除成功");
    }

    /**
     * 获取评论列表
     */
    @Operation(summary = "获取评论列表", description = "获取指定对象的评论列表,包含嵌套回复")
    @GetMapping("/list")
    public Result<PageResult<CommentResponse>> getCommentList(
            @Parameter(description = "评论对象类型") @RequestParam String targetType,
            @Parameter(description = "评论对象ID") @RequestParam Long targetId,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        PageResult<CommentResponse> result = commentService.getCommentList(
                targetType, targetId, page, pageSize
        );
        return Result.success(result);
    }

    /**
     * 获取用户的评论列表
     */
    @Operation(summary = "获取用户评论", description = "获取指定用户的所有评论")
    @GetMapping("/user/{userId}")
    public Result<PageResult<CommentResponse>> getUserComments(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        PageResult<CommentResponse> result = commentService.getUserComments(userId, page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取我的评论列表
     */
    @Operation(summary = "获取我的评论", description = "获取当前用户的所有评论")
    @GetMapping("/my")
    public Result<PageResult<CommentResponse>> getMyComments(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        PageResult<CommentResponse> result = commentService.getUserComments(userId, page, pageSize);
        return Result.success(result);
    }

    /**
     * 点赞评论
     */
    @Operation(summary = "点赞评论")
    @PostMapping("/{id}/like")
    public Result<Map<String, Integer>> likeComment(
            @Parameter(description = "评论ID") @PathVariable Long id
    ) {
        Integer likes = commentService.likeComment(id);
        return Result.success("点赞成功", Map.of("likes", likes));
    }

    /**
     * 取消点赞
     */
    @Operation(summary = "取消点赞")
    @DeleteMapping("/{id}/like")
    public Result<Map<String, Integer>> unlikeComment(
            @Parameter(description = "评论ID") @PathVariable Long id
    ) {
        Integer likes = commentService.unlikeComment(id);
        return Result.success("取消点赞成功", Map.of("likes", likes));
    }

    /**
     * 获取评论数量
     */
    @Operation(summary = "获取评论数量", description = "获取指定对象的评论总数")
    @GetMapping("/count")
    public Result<Map<String, Long>> getCommentCount(
            @Parameter(description = "评论对象类型") @RequestParam String targetType,
            @Parameter(description = "评论对象ID") @RequestParam Long targetId
    ) {
        Long count = commentService.getCommentCount(targetType, targetId);
        return Result.success(Map.of("count", count));
    }
}
