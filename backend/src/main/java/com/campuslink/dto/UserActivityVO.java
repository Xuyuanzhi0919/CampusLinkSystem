package com.campuslink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户最近动态视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityVO {

    /** 动态类型：resource / question / task */
    private String type;

    /** 图标名称（与前端 lucide 图标对应） */
    private String icon;

    /** 动态描述文本 */
    private String text;

    /** 关联目标 ID */
    private Long targetId;

    /** 创建时间（前端自行格式化为相对时间） */
    private LocalDateTime createdAt;

    /** 前端跳转路径 */
    private String path;
}
