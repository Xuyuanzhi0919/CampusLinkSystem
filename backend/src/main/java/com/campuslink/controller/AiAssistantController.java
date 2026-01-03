package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.dto.ai.AiChatRequest;
import com.campuslink.dto.ai.AiChatResponse;
import com.campuslink.service.AiAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
     * 发送消息给 AI 助手（非流式）
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
     * 发送消息给 AI 助手（流式输出 SSE）
     */
    @Operation(summary = "AI 对话（流式）")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @Valid @RequestBody AiChatRequest request
    ) {
        // 设置超时时间为 2 分钟
        SseEmitter emitter = new SseEmitter(120_000L);

        // 异步处理流式响应
        aiAssistantService.chatStream(userId, request, emitter);

        return emitter;
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
