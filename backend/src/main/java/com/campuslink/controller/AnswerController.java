package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 答案控制器
 */
@Tag(name = "答案模块", description = "答案点赞、删除等接口")
@RestController
@RequestMapping("/answer")
@RequiredArgsConstructor
public class AnswerController {
    private final QuestionService questionService;

    @Operation(summary = "点赞答案")
    @PostMapping("/{id}/like")
    public Result<Map<String, Integer>> likeAnswer(
            @Parameter(description = "答案ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        Integer likes = questionService.likeAnswer(userId, id);
        return Result.success("点赞成功", Map.of("likes", likes));
    }

    @Operation(summary = "取消点赞答案")
    @DeleteMapping("/{id}/like")
    public Result<Map<String, Integer>> unlikeAnswer(
            @Parameter(description = "答案ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        Integer likes = questionService.unlikeAnswer(userId, id);
        return Result.success("取消点赞成功", Map.of("likes", likes));
    }

    @Operation(summary = "删除答案")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAnswer(
            @Parameter(description = "答案ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        questionService.deleteAnswer(userId, id);
        return Result.success("删除成功", null);
    }
}
