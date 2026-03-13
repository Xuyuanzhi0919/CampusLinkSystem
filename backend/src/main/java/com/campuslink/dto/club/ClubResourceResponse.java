package com.campuslink.dto.club;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 社团资料响应 DTO
 */
@Data
public class ClubResourceResponse {
    private Long id;
    private String title;
    private String fileSize;
    private String fileType;
    private LocalDateTime uploadTime;
    private String uploaderName;
}
