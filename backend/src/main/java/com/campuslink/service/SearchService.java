package com.campuslink.service;

import com.campuslink.dto.search.HotKeywordResponse;
import com.campuslink.dto.search.SearchSuggestResponse;
import com.campuslink.dto.search.SearchSuggestResponse.SuggestItem;
import com.campuslink.entity.Question;
import com.campuslink.entity.Resource;
import com.campuslink.entity.SearchKeyword;
import com.campuslink.entity.Tag;
import com.campuslink.mapper.QuestionMapper;
import com.campuslink.mapper.ResourceMapper;
import com.campuslink.mapper.SearchKeywordMapper;
import com.campuslink.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索服务类
 * 提供搜索建议、热门搜索词等功能
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {
    private final TagMapper tagMapper;
    private final QuestionMapper questionMapper;
    private final ResourceMapper resourceMapper;
    private final SearchKeywordMapper searchKeywordMapper;

    /**
     * 获取搜索建议
     * 综合返回标签、问题、资源的匹配结果
     *
     * @param keyword 搜索关键词
     * @param limit   每类返回数量，默认5
     * @return 搜索建议响应
     */
    public SearchSuggestResponse getSuggestions(String keyword, Integer limit) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getDefaultSuggestions(limit);
        }

        if (limit == null || limit <= 0) {
            limit = 5;
        }
        if (limit > 10) {
            limit = 10;
        }

        String trimmedKeyword = keyword.trim();

        // 并行获取各类建议
        List<SuggestItem> tagSuggestions = getTagSuggestions(trimmedKeyword, limit);
        List<SuggestItem> questionSuggestions = getQuestionSuggestions(trimmedKeyword, limit);
        List<SuggestItem> resourceSuggestions = getResourceSuggestions(trimmedKeyword, limit);

        return SearchSuggestResponse.builder()
                .tags(tagSuggestions)
                .questions(questionSuggestions)
                .resources(resourceSuggestions)
                .hotWords(new ArrayList<>()) // 暂时返回空，后续可接入热搜词统计
                .build();
    }

    /**
     * 获取默认建议（无关键词时）
     * 返回热门标签和热门搜索词
     */
    private SearchSuggestResponse getDefaultSuggestions(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 8;
        }

        // 获取热门标签作为默认建议
        List<Tag> hotTags = tagMapper.selectHotTags(limit);
        List<SuggestItem> tagSuggestions = hotTags.stream()
                .map(tag -> SuggestItem.builder()
                        .text(tag.getDisplayName() != null ? tag.getDisplayName() : "#" + tag.getTagName())
                        .type("tag")
                        .id(tag.getTagId())
                        .count(tag.getUseCount())
                        .build())
                .collect(Collectors.toList());

        // 热门搜索词（静态示例，后续可接入统计系统）
        List<SuggestItem> hotWords = List.of(
                SuggestItem.builder().text("高数").type("hotWord").count(1280).build(),
                SuggestItem.builder().text("英语四级").type("hotWord").count(956).build(),
                SuggestItem.builder().text("考研").type("hotWord").count(823).build(),
                SuggestItem.builder().text("数据结构").type("hotWord").count(654).build(),
                SuggestItem.builder().text("Python").type("hotWord").count(521).build()
        );

        return SearchSuggestResponse.builder()
                .tags(tagSuggestions)
                .hotWords(hotWords)
                .questions(new ArrayList<>())
                .resources(new ArrayList<>())
                .build();
    }

    /**
     * 获取标签建议
     */
    private List<SuggestItem> getTagSuggestions(String keyword, Integer limit) {
        try {
            List<Tag> tags = tagMapper.searchTags(keyword, limit);
            return tags.stream()
                    .map(tag -> SuggestItem.builder()
                            .text(tag.getDisplayName() != null ? tag.getDisplayName() : "#" + tag.getTagName())
                            .type("tag")
                            .id(tag.getTagId())
                            .count(tag.getUseCount())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取标签建议失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取问题标题建议
     */
    private List<SuggestItem> getQuestionSuggestions(String keyword, Integer limit) {
        try {
            List<Question> questions = questionMapper.searchByTitle(keyword, limit);
            return questions.stream()
                    .map(q -> SuggestItem.builder()
                            .text(q.getTitle())
                            .type("question")
                            .id(q.getQid())
                            .count(q.getViews())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取问题建议失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取资源标题建议
     */
    private List<SuggestItem> getResourceSuggestions(String keyword, Integer limit) {
        try {
            List<Resource> resources = resourceMapper.searchByTitle(keyword, limit);
            return resources.stream()
                    .map(r -> SuggestItem.builder()
                            .text(r.getTitle())
                            .type("resource")
                            .id(r.getRid())
                            .count(r.getDownloads())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取资源建议失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // ==================== 热词排行相关方法 ====================

    /**
     * 获取热门搜索词排行
     *
     * @param limit 返回数量，默认10，最大20
     * @return 热门搜索词列表（带标签和趋势）
     */
    public List<HotKeywordResponse> getHotKeywords(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        if (limit > 20) {
            limit = 20;
        }

        try {
            List<SearchKeyword> keywords = searchKeywordMapper.selectHotKeywords(limit);
            return keywords.stream()
                    .map(this::convertToHotKeywordResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取热门搜索词失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 上报搜索关键词
     * 用于统计搜索行为，更新热词排行
     *
     * @param keyword 搜索关键词
     */
    @Transactional
    public void reportSearchKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        String trimmedKeyword = keyword.trim();
        if (trimmedKeyword.length() > 100) {
            trimmedKeyword = trimmedKeyword.substring(0, 100);
        }

        try {
            // 查询是否已存在
            SearchKeyword existing = searchKeywordMapper.selectByKeyword(trimmedKeyword);
            if (existing != null) {
                // 已存在，增加计数
                searchKeywordMapper.incrementSearchCount(trimmedKeyword);
            } else {
                // 不存在，插入新记录
                searchKeywordMapper.insertKeyword(trimmedKeyword);
            }
        } catch (Exception e) {
            log.error("上报搜索关键词失败: keyword={}, error={}", trimmedKeyword, e.getMessage());
        }
    }

    /**
     * 将 SearchKeyword 实体转换为 HotKeywordResponse DTO
     * 包含标签和趋势的计算逻辑
     */
    private HotKeywordResponse convertToHotKeywordResponse(SearchKeyword keyword) {
        HotKeywordResponse response = HotKeywordResponse.builder()
                .id(keyword.getId())
                .keyword(keyword.getKeyword())
                .searchCount(keyword.getSearchCount())
                .build();

        // 计算标签（tag）
        response.setTag(calculateTag(keyword));

        // 计算趋势（trend）和趋势值
        calculateTrend(keyword, response);

        return response;
    }

    /**
     * 计算热词标签
     * - new: 24小时内首次出现
     * - explode: 今日搜索量占总量 > 20%（爆款）
     * - hot: 搜索量排名前5
     */
    private String calculateTag(SearchKeyword keyword) {
        // 检查是否新上榜（24小时内首次出现）
        if (keyword.getFirstSearchAt() != null) {
            long hoursSinceFirst = ChronoUnit.HOURS.between(keyword.getFirstSearchAt(), LocalDateTime.now());
            if (hoursSinceFirst <= 24) {
                return "new";
            }
        }

        // 检查是否爆款（今日搜索量占总量的比例 > 20%）
        if (keyword.getDailyCount() != null && keyword.getSearchCount() != null && keyword.getSearchCount() > 0) {
            double dailyRatio = (double) keyword.getDailyCount() / keyword.getSearchCount();
            if (dailyRatio > 0.2 && keyword.getDailyCount() >= 10) {
                return "explode";
            }
        }

        // 检查是否热门（排名前5）
        if (keyword.getCurrentRank() != null && keyword.getCurrentRank() <= 5) {
            return "hot";
        }

        return null;
    }

    /**
     * 计算趋势方向和趋势值
     */
    private void calculateTrend(SearchKeyword keyword, HotKeywordResponse response) {
        // 如果没有上次排名记录，视为新上榜
        if (keyword.getLastRank() == null) {
            response.setTrend("new");
            response.setTrendValue(null);
            return;
        }

        // 如果没有当前排名，无法计算趋势
        if (keyword.getCurrentRank() == null) {
            response.setTrend("stable");
            response.setTrendValue(0);
            return;
        }

        int diff = keyword.getLastRank() - keyword.getCurrentRank();
        if (diff > 0) {
            // 排名上升（数字变小）
            response.setTrend("up");
            response.setTrendValue(diff);
        } else if (diff < 0) {
            // 排名下降（数字变大）
            response.setTrend("down");
            response.setTrendValue(diff);
        } else {
            // 排名不变
            response.setTrend("stable");
            response.setTrendValue(0);
        }
    }
}
