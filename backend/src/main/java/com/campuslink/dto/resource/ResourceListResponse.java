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
    private Integer views;  // 浏览次数
    private Integer downloads;
    private Integer likes;
    private Integer favorites;  // 收藏数
    private Double averageRating;  // 平均评分
    private Integer totalRatings;  // 总评分人数

    /**
     * 审核状态：0-待审核，1-已通过，2-已拒绝（仅在获取自己上传的资源时返回）
     */
    private Integer status;

    /**
     * 拒绝原因（仅在status=2时有值）
     */
    private String rejectReason;

    /**
     * 是否已下载（登录用户返回true/false，未登录用户返回false）
     */
    private Boolean isDownloaded;

    /**
     * 是否已点赞（登录用户返回true/false，未登录用户返回false）
     */
    private Boolean isLiked;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
