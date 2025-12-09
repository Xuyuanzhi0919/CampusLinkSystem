package com.campuslink.service;

import com.campuslink.dto.search.SearchSuggestResponse;
import com.campuslink.dto.search.SearchSuggestResponse.SuggestItem;
import com.campuslink.entity.Question;
import com.campuslink.entity.Resource;
import com.campuslink.entity.Tag;
import com.campuslink.mapper.QuestionMapper;
import com.campuslink.mapper.ResourceMapper;
import com.campuslink.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
