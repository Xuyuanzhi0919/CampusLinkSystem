package com.campuslink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新用户资料请求
 */
@Data
@Schema(description = "更新用户资料请求")
public class UpdateProfileRequest {

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    private String nickname;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL")
    private String avatarUrl;

    /**
     * 学号/工号
     */
    @Schema(description = "学号/工号")
    private String studentId;

    /**
     * 专业
     */
    @Schema(description = "专业")
    private String major;

    /**
     * 年级
     */
    @Schema(description = "年级")
    private Integer grade;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;
}
