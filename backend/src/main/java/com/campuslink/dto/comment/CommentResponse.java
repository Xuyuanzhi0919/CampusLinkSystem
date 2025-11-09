package com.campuslink.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论响应DTO
 */
@Data
public class CommentResponse {

    private Long commentId;

    private Long userId;

    private String userName;

    private String userAvatar;

    private String targetType;

    private Long targetId;

    private String content;

    private Long parentId;

    /**
     * 父评论的用户名（回复时有值）
     */
    private String parentUserName;

    private Integer likes;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    /**
     * 回复列表（嵌套评论）
     */
    private List<CommentResponse> replies;

    /**
     * 回复数量
     */
    private Integer replyCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
