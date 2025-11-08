package com.campuslink.dto.resource;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源响应DTO
 */
@Data
public class ResourceResponse {
    private Long resourceId;
    private String title;
    private String description;
    private Long uploaderId;
    private String uploaderName;
    private String uploaderAvatar;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private String category;
    private String courseName;
    private Long schoolId;
    private String schoolName;
    private Integer score;
    private Integer downloads;
    private Integer likes;
    private Boolean isDownloaded;
    private Boolean isLiked;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
