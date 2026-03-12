package com.campuslink.dto.admin;

import lombok.Data;

/**
 * 管理端内容列表查询参数
 */
@Data
public class AdminContentQueryRequest {
    /** 搜索关键词 */
    private String keyword;
    /** 状态筛选 */
    private Integer status;
    /** 页码 */
    private Integer page = 1;
    /** 每页数量 */
    private Integer pageSize = 20;
}
