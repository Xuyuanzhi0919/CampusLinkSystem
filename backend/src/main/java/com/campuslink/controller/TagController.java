package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.dto.tag.TagResponse;
import com.campuslink.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签控制器
 * 提供热门标签、标签搜索等接口
 */
@Tag(name = "标签模块", description = "热门标签、标签搜索等接口")
@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    /**
     * 获取热门标签列表
     * GET /tag/hot?limit=20
     */
    @Operation(summary = "获取热门标签列表", description = "根据使用次数返回最热门的标签，默认返回20个")
    @GetMapping("/hot")
    public Result<List<TagResponse>> getHotTags(
            @Parameter(description = "返回数量，默认20，最大50")
            @RequestParam(required = false, defaultValue = "20") Integer limit
    ) {
        List<TagResponse> tags = tagService.getHotTags(limit);
        return Result.success(tags);
    }

    /**
     * 根据分类获取热门标签
     * GET /tag/hot/category?category=学习&limit=10
     */
    @Operation(summary = "根据分类获取热门标签", description = "获取指定分类下的热门标签")
    @GetMapping("/hot/category")
    public Result<List<TagResponse>> getHotTagsByCategory(
            @Parameter(description = "标签分类：学习/生活/技术/娱乐")
            @RequestParam String category,
            @Parameter(description = "返回数量，默认10，最大50")
            @RequestParam(required = false, defaultValue = "10") Integer limit
    ) {
        List<TagResponse> tags = tagService.getHotTagsByCategory(category, limit);
        return Result.success(tags);
    }

    /**
     * 搜索标签
     * GET /tag/search?keyword=考研&limit=10
     */
    @Operation(summary = "搜索标签", description = "根据关键词模糊匹配标签名称")
    @GetMapping("/search")
    public Result<List<TagResponse>> searchTags(
            @Parameter(description = "搜索关键词")
            @RequestParam String keyword,
            @Parameter(description = "返回数量，默认10，最大30")
            @RequestParam(required = false, defaultValue = "10") Integer limit
    ) {
        List<TagResponse> tags = tagService.searchTags(keyword, limit);
        return Result.success(tags);
    }

    /**
     * 获取目标对象关联的标签
     * GET /tag/target?targetType=question&targetId=1001
     */
    @Operation(summary = "获取目标对象的标签", description = "获取问题、资源或任务关联的标签列表")
    @GetMapping("/target")
    public Result<List<TagResponse>> getTagsByTarget(
            @Parameter(description = "目标类型：question/resource/task")
            @RequestParam String targetType,
            @Parameter(description = "目标ID")
            @RequestParam Long targetId
    ) {
        List<TagResponse> tags = tagService.getTagsByTarget(targetType, targetId);
        return Result.success(tags);
    }
}
