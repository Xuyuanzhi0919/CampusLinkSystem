package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签实体类
 * 用于管理平台所有标签及其热度统计
 */
@Data
@TableName("tag")
public class Tag {
    /**
     * 标签ID
     */
    @TableId(value = "tag_id", type = IdType.AUTO)
    private Long tagId;

    /**
     * 标签名称（不含#号）
     * 例如："考研资料"
     */
    @TableField("tag_name")
    private String tagName;

    /**
     * 显示名称（含#号）
     * 例如："#考研资料"
     */
    @TableField("display_name")
    private String displayName;

    /**
     * 使用次数（热度）
     * 每次标签被使用时增加
     */
    @TableField("use_count")
    private Integer useCount;

    /**
     * 标签分类
     * 学习/生活/技术/娱乐等
     */
    private String category;

    /**
     * 标签描述
     */
    private String description;

    /**
     * 状态：0-禁用，1-正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
