package com.campuslink.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端用户视图对象（包含管理员需要的额外字段）
 */
@Data
public class AdminUserVO {
    @JsonProperty("uId")
    private Long uId;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatarUrl;
    private String role;
    private String studentId;
    private Long schoolId;
    private String schoolName;
    private String major;
    private Integer grade;
    private Integer points;
    private Integer level;
    private Double creditScore;
    private Integer ratingCount;
    /** 账号状态：0-禁用，1-正常 */
    private Integer status;
    /** 是否实名认证：0-否，1-是 */
    private Integer isVerified;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createdAt;
}
