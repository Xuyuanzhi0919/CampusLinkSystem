package com.campuslink.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下载资源响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadResourceResponse {
    private String downloadUrl;
    private Integer pointsCost;
    private Integer remainingPoints;
}
