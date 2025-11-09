package com.campuslink.dto.download;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 下载记录响应DTO
 */
@Data
public class DownloadLogResponse {

    private Long dlId;

    private Long userId;

    private String userName;

    private String userAvatar;

    private Long resourceId;

    private String resourceTitle;

    private String fileType;

    private Integer pointsCost;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
