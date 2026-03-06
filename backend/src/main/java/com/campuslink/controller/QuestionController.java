package com.campuslink.controller;

import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.dto.question.*;
import com.campuslink.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 问答控制器
 */
@Tag(name = "问答模块", description = "提问、回答、采纳、点赞等接口")
@RestController
@RequestMapping("/question")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    @Operation(summary = "提问")
    @PostMapping("/ask")
    public Result<Map<String, Long>> askQuestion(
            @Valid @RequestBody AskQuestionRequest request,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        Long questionId = questionService.askQuestion(userId, request);
        return Result.success("提问成功", Map.of("questionId", questionId));
    }

    @Operation(summary = "获取问题列表")
    @GetMapping("/list")
    public Result<PageResult<QuestionListResponse>> getQuestionList(
            @Parameter(description = "问题分类") @RequestParam(required = false) String category,
            @Parameter(description = "学校ID") @RequestParam(required = false) Long schoolId,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "是否已解决（0=未解决，1=已解决）") @RequestParam(required = false) Integer isSolved,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "created_at") String sortBy,
            @Parameter(description = "排序方式") @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        PageResult<QuestionListResponse> result = questionService.getQuestionList(
                category, schoolId, keyword, isSolved, page, pageSize, sortBy, sortOrder
        );
        return Result.success(result);
    }

    @Operation(summary = "获取我的提问")
    @GetMapping("/my")
    public Result<PageResult<QuestionListResponse>> getMyQuestions(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "问题状态") @RequestParam(required = false) Integer status,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        PageResult<QuestionListResponse> result = questionService.getMyQuestions(userId, page, pageSize, status);
        return Result.success(result);
    }

    @Operation(summary = "获取问题详情")
    @GetMapping("/{id}")
    public Result<QuestionResponse> getQuestionById(
            @Parameter(description = "问题ID") @PathVariable Long id
    ) {
        QuestionResponse question = questionService.getQuestionById(id);
        return Result.success(question);
    }

    @Operation(summary = "更新问题")
    @PutMapping("/{id}")
    public Result<Map<String, Long>> updateQuestion(
            @Parameter(description = "问题ID") @PathVariable Long id,
            @Valid @RequestBody AskQuestionRequest request,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        questionService.updateQuestion(userId, id, request);
        return Result.success("更新成功", Map.of("questionId", id));
    }

    @Operation(summary = "回答问题")
    @PostMapping("/{id}/answer")
    public Result<Map<String, Long>> answerQuestion(
            @Parameter(description = "问题ID") @PathVariable Long id,
            @Valid @RequestBody AnswerQuestionRequest request,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        Long answerId = questionService.answerQuestion(userId, id, request);
        return Result.success("回答成功", Map.of("answerId", answerId));
    }

    @Operation(summary = "获取问题的所有答案")
    @GetMapping("/{id}/answers")
    public Result<List<AnswerResponse>> getAnswers(
            @Parameter(description = "问题ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId
    ) {
        List<AnswerResponse> answers = questionService.getAnswersByQuestionId(id, userId);
        return Result.success(answers);
    }

    @Operation(summary = "采纳答案")
    @PostMapping("/{questionId}/accept/{answerId}")
    public Result<Void> acceptAnswer(
            @Parameter(description = "问题ID") @PathVariable Long questionId,
            @Parameter(description = "答案ID") @PathVariable Long answerId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        questionService.acceptAnswer(userId, questionId, answerId);
        return Result.success("采纳成功", null);
    }

    @Operation(summary = "点赞问题")
    @PostMapping("/{id}/like")
    public Result<Map<String, Integer>> likeQuestion(
            @Parameter(description = "问题ID") @PathVariable Long id
    ) {
        Integer likes = questionService.likeQuestion(id);
        return Result.success("点赞成功", Map.of("likes", likes));
    }

    @Operation(summary = "取消点赞问题")
    @DeleteMapping("/{id}/like")
    public Result<Map<String, Integer>> unlikeQuestion(
            @Parameter(description = "问题ID") @PathVariable Long id
    ) {
        Integer likes = questionService.unlikeQuestion(id);
        return Result.success("取消点赞成功", Map.of("likes", likes));
    }

    @Operation(summary = "删除问题")
    @DeleteMapping("/{id}")
    public Result<Void> deleteQuestion(
            @Parameter(description = "问题ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        questionService.deleteQuestion(userId, id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "获取热门标签")
    @GetMapping("/hot-tags")
    public Result<List<HotTagResponse>> getHotTags(
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "8") Integer limit
    ) {
        List<HotTagResponse> tags = questionService.getHotTags(limit);
        return Result.success(tags);
    }

    @Operation(summary = "获取活跃答主")
    @GetMapping("/active-users")
    public Result<List<ActiveUserResponse>> getActiveUsers(
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "4") Integer limit,
            @Parameter(description = "时间范围（7d/30d）") @RequestParam(defaultValue = "7d") String period
    ) {
        List<ActiveUserResponse> users = questionService.getActiveUsers(limit, period);
        return Result.success(users);
    }

    @Operation(summary = "获取精选问题列表", description = "用于首页推荐位轮播，返回多条综合质量最高的问题")
    @GetMapping("/featured")
    public Result<List<FeaturedQuestionResponse>> getFeaturedQuestions(
            @Parameter(description = "返回数量，默认5条") @RequestParam(defaultValue = "5") Integer limit
    ) {
        List<FeaturedQuestionResponse> questions = questionService.getFeaturedQuestions(limit);
        return Result.success(questions);
    }
}
