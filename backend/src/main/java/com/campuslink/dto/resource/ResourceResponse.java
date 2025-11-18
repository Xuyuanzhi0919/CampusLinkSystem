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
    private Integer views;  // 浏览次数
    private Integer downloads;
    private Integer likes;
    private Integer uploaderPoints;  // 上传者积分
    private Integer commentCount;  // 评论数量
    private Boolean isDownloaded;
    private Boolean isLiked;
    private Boolean isFavorited;  // 是否已收藏
    private Integer favorites;  // 收藏数
    private Double averageRating;  // 平均评分（1-5）
    private Integer totalRatings;  // 总评分人数
    private Integer userRating;  // 用户评分（0表示未评分）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
