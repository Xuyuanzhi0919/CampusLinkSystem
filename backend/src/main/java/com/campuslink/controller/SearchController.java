package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.dto.search.HotKeywordResponse;
import com.campuslink.dto.search.SearchReportRequest;
import com.campuslink.dto.search.SearchSuggestResponse;
import com.campuslink.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索控制器
 * 提供搜索建议、热门搜索词等接口
 */
@Tag(name = "搜索模块", description = "搜索建议、热门搜索等接口")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    /**
     * 获取搜索建议
     * GET /search/suggest?keyword=考研&limit=5
     */
    @Operation(summary = "获取搜索建议", description = "根据关键词返回标签、问题、资源的联想建议")
    @GetMapping("/suggest")
    public Result<SearchSuggestResponse> getSuggestions(
            @Parameter(description = "搜索关键词，为空时返回热门推荐")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "每类返回数量，默认5，最大10")
            @RequestParam(required = false, defaultValue = "5") Integer limit
    ) {
        SearchSuggestResponse suggestions = searchService.getSuggestions(keyword, limit);
        return Result.success(suggestions);
    }

    /**
     * 获取热门搜索词排行
     * GET /search/hot-keywords?limit=10
     */
    @Operation(summary = "获取热门搜索词", description = "返回热门搜索词排行榜，包含标签（新/热/爆）和趋势信息")
    @GetMapping("/hot-keywords")
    public Result<List<HotKeywordResponse>> getHotKeywords(
            @Parameter(description = "返回数量，默认10，最大20")
            @RequestParam(required = false, defaultValue = "10") Integer limit
    ) {
        List<HotKeywordResponse> hotKeywords = searchService.getHotKeywords(limit);
        return Result.success(hotKeywords);
    }

    /**
     * 上报搜索关键词
     * POST /search/report
     * 用于统计用户搜索行为，更新热词排行
     */
    @Operation(summary = "上报搜索关键词", description = "记录用户搜索行为，用于热词统计")
    @PostMapping("/report")
    public Result<Void> reportSearch(
            @Valid @RequestBody SearchReportRequest request
    ) {
        searchService.reportSearchKeyword(request.getKeyword());
        return Result.success();
    }
}
