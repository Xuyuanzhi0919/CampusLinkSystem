package com.campuslink.controller;

import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.dto.message.ConversationResponse;
import com.campuslink.dto.message.MessageResponse;
import com.campuslink.dto.message.SendMessageRequest;
import com.campuslink.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 私信管理接口
 */
@Tag(name = "私信管理")
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 发送私信
     */
    @Operation(summary = "发送私信")
    @PostMapping("/send")
    public Result<Long> sendMessage(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        Long messageId = messageService.sendMessage(userId, request);
        return Result.success("发送成功", messageId);
    }

    /**
     * 获取与某个用户的聊天记录
     */
    @Operation(summary = "获取聊天记录")
    @GetMapping("/chat/{otherUserId}")
    public Result<PageResult<MessageResponse>> getChatHistory(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long otherUserId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        PageResult<MessageResponse> result = messageService.getChatHistory(userId, otherUserId, page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取会话列表
     */
    @Operation(summary = "获取会话列表")
    @GetMapping("/conversations")
    public Result<List<ConversationResponse>> getConversationList(
            @RequestAttribute("userId") Long userId
    ) {
        List<ConversationResponse> conversations = messageService.getConversationList(userId);
        return Result.success(conversations);
    }

    /**
     * 标记消息为已读
     */
    @Operation(summary = "标记消息为已读")
    @PutMapping("/read/{otherUserId}")
    public Result<Void> markAsRead(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long otherUserId
    ) {
        messageService.markAsRead(userId, otherUserId);
        return Result.success("标记成功");
    }

    /**
     * 获取未读消息总数
     */
    @Operation(summary = "获取未读消息总数")
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(
            @RequestAttribute("userId") Long userId
    ) {
        Integer count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 删除消息
     */
    @Operation(summary = "删除消息")
    @DeleteMapping("/{messageId}")
    public Result<Void> deleteMessage(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long messageId
    ) {
        messageService.deleteMessage(userId, messageId);
        return Result.success("删除成功");
    }
}
