package com.campuslink.dto.club;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 社团动态响应 DTO
 */
@Data
public class ClubPostResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userAvatar;
    private String content;
    private LocalDateTime createdAt;
    private Integer likes;
    private Integer comments;
}
