package com.campuslink.dto.message;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 发送私信请求
 */
@Data
public class SendMessageRequest {
    /**
     * 接收者ID
     */
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /**
     * 消息类型：1-文本，2-图片，3-文件
     */
    @NotNull(message = "消息类型不能为空")
    @Min(value = 1, message = "消息类型无效")
    @Max(value = 3, message = "消息类型无效")
    private Integer msgType;
}
