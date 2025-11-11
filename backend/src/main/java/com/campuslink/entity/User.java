package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User {

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long uId;

    /**
     * 用户名（唯一）
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱（唯一）
     */
    private String email;

    /**
     * 手机号（唯一）
     */
    private String phone;

    /**
     * 密码哈希（加密存储）
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 头像URL
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 学号/工号
     */
    private String studentId;

    /**
     * 所属学校ID
     */
    private Long schoolId;

    /**
     * 专业
     */
    private String major;

    /**
     * 年级
     */
    private Integer grade;

    /**
     * 用户角色：student-学生，teacher-教师，admin-管理员
     */
    private String role;

    /**
     * 积分
     */
    private Integer points;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 信用评分(0.0-5.0)
     */
    @TableField("credit_score")
    private Double creditScore;

    /**
     * 被评价次数
     */
    @TableField("rating_count")
    private Integer ratingCount;

    /**
     * 账号状态：0-禁用，1-正常
     */
    private Integer status;

    /**
     * 是否实名认证：0-否，1-是
     */
    private Integer isVerified;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
