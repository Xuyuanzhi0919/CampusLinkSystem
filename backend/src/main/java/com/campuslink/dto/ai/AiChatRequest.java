package com.campuslink.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * AI 对话请求
 */
@Data
public class AiChatRequest {

    /**
     * 用户消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容不能超过2000字")
    private String message;

    /**
     * 对话历史（可选，用于上下文推理）
     */
    private List<ChatMessage> history;

    /**
     * 消息分类（可选）
     */
    private String category;

    @Data
    public static class ChatMessage {
        /**
         * 角色：user 或 assistant
         */
        private String role;

        /**
         * 消息内容
         */
        private String content;
    }
}
