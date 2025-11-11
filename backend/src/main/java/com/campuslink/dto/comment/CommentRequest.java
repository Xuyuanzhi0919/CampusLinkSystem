package com.campuslink.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发表评论请求
 */
@Data
public class CommentRequest {
    /**
     * 评论对象类型（task/resource/question/answer等）
     */
    @NotBlank(message = "评论对象类型不能为空")
    private String targetType;

    /**
     * 评论对象ID
     */
    @NotNull(message = "评论对象ID不能为空")
    private Long targetId;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;

    /**
     * 父评论ID（用于回复，可选）
     */
    private Long parentId;
}
