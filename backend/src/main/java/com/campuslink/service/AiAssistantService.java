package com.campuslink.service;

import com.campuslink.dto.ai.AiChatRequest;
import com.campuslink.dto.ai.AiChatResponse;

/**
 * AI 智能助手服务接口
 */
public interface AiAssistantService {

    /**
     * AI 对话
     *
     * @param userId  用户 ID（可为空，未登录也可使用）
     * @param request 对话请求
     * @return AI 回复
     */
    AiChatResponse chat(Long userId, AiChatRequest request);
}
