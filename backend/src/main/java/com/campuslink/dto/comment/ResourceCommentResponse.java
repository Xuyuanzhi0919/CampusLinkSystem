package com.campuslink.dto.comment;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ResourceCommentResponse {

    private Long commentId;
    private Long resourceId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Long parentId;
    private String content;
    private LocalDateTime createdAt;
    private List<ResourceCommentResponse> replies;
    private Integer replyCount;
}
