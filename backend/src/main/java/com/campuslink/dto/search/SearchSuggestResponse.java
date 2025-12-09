package com.campuslink.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 搜索建议响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchSuggestResponse {
    /**
     * 热门标签建议
     */
    private List<SuggestItem> tags;

    /**
     * 历史热搜建议
     */
    private List<SuggestItem> hotWords;

    /**
     * 相关问题标题建议
     */
    private List<SuggestItem> questions;

    /**
     * 相关资源标题建议
     */
    private List<SuggestItem> resources;

    /**
     * 建议项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SuggestItem {
        /**
         * 建议文本
         */
        private String text;

        /**
         * 类型：tag/hotWord/question/resource
         */
        private String type;

        /**
         * 关联ID（可选，用于直接跳转）
         */
        private Long id;

        /**
         * 热度/使用次数（可选）
         */
        private Integer count;
    }
}
