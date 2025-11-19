package com.campuslink.dto.comment;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

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
    private String parentUserName;
    private Integer likes;
    private Boolean isLiked;
    private LocalDateTime createdAt;
    private List<CommentResponse> replies;
    private Integer replyCount;
}
