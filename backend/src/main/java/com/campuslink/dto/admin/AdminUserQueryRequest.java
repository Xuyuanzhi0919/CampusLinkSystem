package com.campuslink.dto.admin;

import lombok.Data;

/**
 * 管理端用户列表查询参数
 */
@Data
public class AdminUserQueryRequest {
    /** 搜索关键词（用户名/昵称/邮箱） */
    private String keyword;
    /** 角色筛选：student/teacher/admin */
    private String role;
    /** 状态筛选：0-禁用，1-正常 */
    private Integer status;
    /** 页码，默认1 */
    private Integer page = 1;
    /** 每页数量，默认20 */
    private Integer pageSize = 20;
    /** 排序字段：createdAt/points/lastLoginTime，默认 createdAt */
    private String sortBy = "createdAt";
    /** 排序方向：asc/desc，默认 desc */
    private String sortOrder = "desc";
}
