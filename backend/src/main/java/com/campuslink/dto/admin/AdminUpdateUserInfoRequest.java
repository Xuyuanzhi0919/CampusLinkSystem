package com.campuslink.dto.admin;

import lombok.Data;

/**
 * 管理员修改用户基本信息请求
 */
@Data
public class AdminUpdateUserInfoRequest {

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 专业 */
    private String major;

    /** 年级 */
    private Integer grade;

    /** 学号/工号 */
    private String studentId;
}
