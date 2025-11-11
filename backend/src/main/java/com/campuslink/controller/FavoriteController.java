package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 收藏控制器
 */
@Tag(name = "收藏管理", description = "收藏、取消收藏等接口")
@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    /**
     * 添加收藏
     */
    @Operation(summary = "添加收藏", description = "收藏任务、资源、问题等")
    @PostMapping("/{targetType}/{targetId}")
    public Result<Void> addFavorite(
            @Parameter(description = "对象类型(task/resource/question)") @PathVariable String targetType,
            @Parameter(description = "对象ID") @PathVariable Long targetId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        favoriteService.addFavorite(userId, targetType, targetId);
        return Result.success("收藏成功");
    }

    /**
     * 取消收藏
     */
    @Operation(summary = "取消收藏")
    @DeleteMapping("/{targetType}/{targetId}")
    public Result<Void> removeFavorite(
            @Parameter(description = "对象类型(task/resource/question)") @PathVariable String targetType,
            @Parameter(description = "对象ID") @PathVariable Long targetId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        favoriteService.removeFavorite(userId, targetType, targetId);
        return Result.success("取消收藏成功");
    }

    /**
     * 检查是否已收藏
     */
    @Operation(summary = "检查是否已收藏")
    @GetMapping("/check/{targetType}/{targetId}")
    public Result<Map<String, Boolean>> checkFavorite(
            @Parameter(description = "对象类型") @PathVariable String targetType,
            @Parameter(description = "对象ID") @PathVariable Long targetId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        boolean isFavorited = favoriteService.isFavorited(userId, targetType, targetId);
        return Result.success(Map.of("isFavorited", isFavorited));
    }

    /**
     * 获取收藏数
     */
    @Operation(summary = "获取收藏数", description = "获取指定对象的收藏总数")
    @GetMapping("/count/{targetType}/{targetId}")
    public Result<Map<String, Long>> getFavoriteCount(
            @Parameter(description = "对象类型") @PathVariable String targetType,
            @Parameter(description = "对象ID") @PathVariable Long targetId
    ) {
        Long count = favoriteService.getFavoriteCount(targetType, targetId);
        return Result.success(Map.of("count", count));
    }
}
