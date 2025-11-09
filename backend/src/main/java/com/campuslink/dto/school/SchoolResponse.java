package com.campuslink.dto.school;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学校响应DTO
 */
@Data
public class SchoolResponse {

    private Long schoolId;

    private String schoolName;

    private String province;

    private String city;

    private String address;

    private String logoUrl;

    /**
     * 状态：0-禁用，1-正常
     */
    private Integer status;

    private String statusText;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public String getStatusText() {
        if (status == null) {
            return "未知";
        }
        return status == 1 ? "正常" : "禁用";
    }
}
