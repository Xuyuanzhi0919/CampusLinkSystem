package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.dto.ai.AiChatRequest;
import com.campuslink.dto.ai.AiChatResponse;
import com.campuslink.service.AiAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AI 智能助手接口
 */
@Tag(name = "AI 智能助手")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    /**
     * 发送消息给 AI 助手
     */
    @Operation(summary = "AI 对话")
    @PostMapping("/chat")
    public Result<AiChatResponse> chat(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @Valid @RequestBody AiChatRequest request
    ) {
        AiChatResponse response = aiAssistantService.chat(userId, request);
        return Result.success(response);
    }

    /**
     * 健康检查
     */
    @Operation(summary = "AI 服务健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("AI 服务运行正常");
    }
}
