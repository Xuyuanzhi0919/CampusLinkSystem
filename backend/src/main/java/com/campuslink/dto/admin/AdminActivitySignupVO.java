package com.campuslink.dto.admin;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminActivitySignupVO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private Integer isSignedIn;   // 0-未签到 1-已签到
    private LocalDateTime signedInAt;
    private LocalDateTime joinedAt;
}
