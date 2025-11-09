package com.campuslink.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建评论请求DTO
 */
@Data
public class CreateCommentRequest {

    @NotBlank(message = "评论对象类型不能为空")
    @Size(max = 50, message = "评论对象类型最多50个字符")
    private String targetType;

    @NotNull(message = "评论对象ID不能为空")
    private Long targetId;

    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 1000, message = "评论内容长度为1-1000个字符")
    private String content;

    /**
     * 父评论ID（回复时填写）
     */
    private Long parentId;
}
