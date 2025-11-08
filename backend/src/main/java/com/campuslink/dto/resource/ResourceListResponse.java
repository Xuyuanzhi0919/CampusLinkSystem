package com.campuslink.dto.resource;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源列表响应DTO（精简版）
 */
@Data
public class ResourceListResponse {
    private Long resourceId;
    private String title;
    private String description;
    private String uploaderName;
    private String uploaderAvatar;
    private String fileType;
    private Long fileSize;
    private String category;
    private String courseName;
    private Integer score;
    private Integer downloads;
    private Integer likes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
