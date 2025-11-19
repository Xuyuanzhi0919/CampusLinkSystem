package com.campuslink.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象（不包含敏感信息）
 */
@Data
public class UserVO {

    /**
     * 用户ID
     */
    private Long uId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号（脱敏）
     */
    private String phone;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 角色
     */
    private String role;

    /**
     * 学号/工号
     */
    private String studentId;

    /**
     * 所属学校ID
     */
    private Long schoolId;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 专业
     */
    private String major;

    /**
     * 年级
     */
    private Integer grade;

    /**
     * 积分
     */
    private Integer points;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
