package com.campuslink.dto.user;

import com.campuslink.validation.ValidNickname;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新个人资料请求
 */
@Data
public class UpdateProfileRequest {
    /**
     * 昵称
     */
    @Size(max = 20, message = "昵称最多20个字符")
    @ValidNickname
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 专业
     */
    private String major;

    /**
     * 年级
     */
    private Integer grade;
}
