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

    /**
     * 审核状态：0-待审核，1-已通过，2-已拒绝（仅在获取自己上传的资源时返回）
     */
    private Integer status;

    /**
     * 拒绝原因（仅在status=2时有值）
     */
    private String rejectReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
