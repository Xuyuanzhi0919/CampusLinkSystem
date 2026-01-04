package com.campuslink.service.impl;

import com.campuslink.dto.ai.AiChatRequest;
import com.campuslink.dto.ai.AiChatResponse;
import com.campuslink.exception.BusinessException;
import com.campuslink.service.AiAssistantService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * AI 智能助手服务实现（DeepSeek API）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    @Value("${deepseek.api.key:}")
    private String apiKey;

    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    @Value("${deepseek.max-tokens:2000}")
    private Integer maxTokens;

    @Value("${deepseek.temperature:0.7}")
    private Double temperature;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public AiChatResponse chat(Long userId, AiChatRequest request) {
        // 检查 API Key 配置
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("DeepSeek API Key 未配置，使用模拟响应");
            return getMockResponse(request);
        }

        try {
            // 构建消息列表
            ArrayNode messages = objectMapper.createArrayNode();

            // 添加系统提示
            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", getSystemPrompt());
            messages.add(systemMessage);

            // 添加历史对话（限制最近10轮）
            if (request.getHistory() != null && !request.getHistory().isEmpty()) {
                List<AiChatRequest.ChatMessage> history = request.getHistory();
                int startIndex = Math.max(0, history.size() - 20); // 最多保留10轮（20条消息）
                for (int i = startIndex; i < history.size(); i++) {
                    AiChatRequest.ChatMessage msg = history.get(i);
                    ObjectNode historyMessage = objectMapper.createObjectNode();
                    historyMessage.put("role", msg.getRole());
                    historyMessage.put("content", msg.getContent());
                    messages.add(historyMessage);
                }
            }

            // 添加用户当前消息
            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", request.getMessage());
            messages.add(userMessage);

            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.set("messages", messages);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);
            requestBody.put("stream", false);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(
                    objectMapper.writeValueAsString(requestBody),
                    headers
            );

            // 发送请求
            log.info("发送 DeepSeek API 请求，用户 ID: {}", userId);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 解析响应
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String content = responseJson
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();

            Integer tokensUsed = responseJson
                    .path("usage")
                    .path("total_tokens")
                    .asInt(0);

            log.info("DeepSeek API 响应成功，Token 使用: {}", tokensUsed);

            return AiChatResponse.builder()
                    .content(content)
                    .category(request.getCategory())
                    .messageId(UUID.randomUUID().toString())
                    .tokensUsed(tokensUsed)
                    .build();

        } catch (Exception e) {
            log.error("调用 DeepSeek API 失败", e);
            throw new BusinessException("AI 服务暂时不可用，请稍后再试");
        }
    }

    /**
     * 获取系统提示词
     */
    private String getSystemPrompt() {
        return """
                你是 CampusLink 平台的 AI 学习助手，一个专注于帮助大学生学习的智能助手。

                你的职责：
                1. 解答学习相关问题（课程内容、考试复习、编程技术等）
                2. 推荐学习资源和学习方法
                3. 提供学习建议和规划指导
                4. 解决技术问题和编程难题

                回复要求：
                - 使用简洁、友好的中文回复
                - 支持 Markdown 格式（代码块、列表、加粗等）
                - 提供结构化、易读的答案
                - 对于编程问题，提供代码示例并加以说明
                - 对于复杂问题，分步骤解答

                注意事项：
                - 如果问题超出学习范畴，礼貌地引导用户回到学习话题
                - 不要提供作业答案，而是引导用户思考
                - 鼓励自主学习和独立思考
                """;
    }

    /**
     * AI 对话（流式输出 SSE）
     */
    @Override
    @Async
    public void chatStream(Long userId, AiChatRequest request, SseEmitter emitter) {
        // 检查 API Key 配置
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("DeepSeek API Key 未配置，使用模拟流式响应");
            sendMockStreamResponse(request, emitter);
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                // 构建消息列表
                ArrayNode messages = objectMapper.createArrayNode();

                // 添加系统提示
                ObjectNode systemMessage = objectMapper.createObjectNode();
                systemMessage.put("role", "system");
                systemMessage.put("content", getSystemPrompt());
                messages.add(systemMessage);

                // 添加历史对话（限制最近10轮）
                if (request.getHistory() != null && !request.getHistory().isEmpty()) {
                    List<AiChatRequest.ChatMessage> history = request.getHistory();
                    int startIndex = Math.max(0, history.size() - 20);
                    for (int i = startIndex; i < history.size(); i++) {
                        AiChatRequest.ChatMessage msg = history.get(i);
                        ObjectNode historyMessage = objectMapper.createObjectNode();
                        historyMessage.put("role", msg.getRole());
                        historyMessage.put("content", msg.getContent());
                        messages.add(historyMessage);
                    }
                }

                // 添加用户当前消息
                ObjectNode userMessage = objectMapper.createObjectNode();
                userMessage.put("role", "user");
                userMessage.put("content", request.getMessage());
                messages.add(userMessage);

                // 构建请求体（启用流式）
                ObjectNode requestBody = objectMapper.createObjectNode();
                requestBody.put("model", model);
                requestBody.set("messages", messages);
                requestBody.put("max_tokens", maxTokens);
                requestBody.put("temperature", temperature);
                requestBody.put("stream", true); // 启用流式输出

                // 使用 HttpURLConnection 处理流式响应
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                connection.setDoOutput(true);

                // 发送请求
                connection.getOutputStream().write(
                        objectMapper.writeValueAsBytes(requestBody)
                );

                log.info("发送 DeepSeek 流式 API 请求，用户 ID: {}", userId);

                // 读取流式响应
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );

                String line;
                StringBuilder fullContent = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    // SSE 格式: data: {...}
                    if (line.startsWith("data: ")) {
                        String jsonData = line.substring(6); // 移除 "data: " 前缀

                        // 检查是否是结束标记
                        if ("[DONE]".equals(jsonData)) {
                            log.info("DeepSeek 流式响应完成");
                            break;
                        }

                        try {
                            // 解析 JSON
                            JsonNode chunk = objectMapper.readTree(jsonData);
                            String delta = chunk
                                    .path("choices")
                                    .path(0)
                                    .path("delta")
                                    .path("content")
                                    .asText("");

                            if (!delta.isEmpty()) {
                                fullContent.append(delta);

                                // 发送增量数据到前端
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(delta));
                            }
                        } catch (Exception e) {
                            log.warn("解析流式数据块失败: {}", jsonData, e);
                        }
                    }
                }

                reader.close();
                connection.disconnect();

                // 发送完成事件
                emitter.send(SseEmitter.event()
                        .name("done")
                        .data("complete"));

                emitter.complete();

                log.info("DeepSeek 流式响应已完成，总字符数: {}", fullContent.length());

            } catch (Exception e) {
                log.error("DeepSeek 流式 API 调用失败", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("AI 服务暂时不可用，请稍后再试"));
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.error("发送错误事件失败", ex);
                }
            }
        });
    }

    /**
     * 发送模拟流式响应
     */
    private void sendMockStreamResponse(AiChatRequest request, SseEmitter emitter) {
        CompletableFuture.runAsync(() -> {
            try {
                String mockContent = """
                        感谢您的提问！

                        **当前状态**：AI 服务处于演示模式（DeepSeek API Key 未配置）

                        **正式使用方法**：
                        1. 在 `application.yml` 中配置 DeepSeek API Key
                        2. 重启后端服务即可使用真实的 AI 对话功能

                        **配置示例**：
                        ```yaml
                        deepseek:
                          api:
                            key: your-deepseek-api-key
                        ```

                        如需获取 API Key，请访问：https://platform.deepseek.com/
                        """;

                // 模拟逐词输出(按词分割,每次发送3-5个字符)
                int chunkSize = 5; // 每次发送5个字符
                for (int i = 0; i < mockContent.length(); i += chunkSize) {
                    int end = Math.min(i + chunkSize, mockContent.length());
                    String chunk = mockContent.substring(i, end);

                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data(chunk));
                    Thread.sleep(50); // 模拟网络延迟
                }

                // 发送完成事件
                emitter.send(SseEmitter.event()
                        .name("done")
                        .data("complete"));

                emitter.complete();

            } catch (Exception e) {
                log.error("发送模拟流式响应失败", e);
                emitter.completeWithError(e);
            }
        });
    }

    /**
     * 获取模拟响应（API Key 未配置时使用）
     */
    private AiChatResponse getMockResponse(AiChatRequest request) {
        String mockContent = """
                感谢您的提问！

                **当前状态**：AI 服务处于演示模式（DeepSeek API Key 未配置）

                **正式使用方法**：
                1. 在 `application.yml` 中配置 DeepSeek API Key
                2. 重启后端服务即可使用真实的 AI 对话功能

                **配置示例**：
                ```yaml
                deepseek:
                  api:
                    key: your-deepseek-api-key
                ```

                如需获取 API Key，请访问：https://platform.deepseek.com/
                """;

        return AiChatResponse.builder()
                .content(mockContent)
                .category(request.getCategory())
                .messageId(UUID.randomUUID().toString())
                .tokensUsed(0)
                .build();
    }
}
