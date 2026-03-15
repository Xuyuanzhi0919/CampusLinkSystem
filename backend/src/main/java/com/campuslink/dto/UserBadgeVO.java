package com.campuslink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户徽章视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBadgeVO {

    private Integer id;

    private String name;

    /** lucide 图标名称，与前端 Icon 组件对应 */
    private String icon;

    /** 颜色主题：blue | teal | violet | amber | rose */
    private String color;

    /** 分类：growth | contribution | interaction | limited */
    private String category;

    /** 是否已解锁 */
    private Boolean unlocked;

    /** 解锁日期，格式 YYYY-MM-DD；未解锁时为 null */
    private String unlockedAt;

    private String description;

    /** 解锁条件说明 */
    private String condition;

    /** 解锁奖励积分 */
    private Integer reward;
}
