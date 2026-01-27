package com.campuslink.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 对话响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {

    /**
     * AI 回复内容
     */
    private String content;

    /**
     * 消息分类
     */
    private String category;

    /**
     * 消息 ID（用于反馈）
     */
    private String messageId;

    /**
     * Token 使用量（可选）
     */
    private Integer tokensUsed;
}
