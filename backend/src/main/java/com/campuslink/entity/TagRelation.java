package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签关联实体类
 * 记录标签与具体内容（问题、资源等）的关联关系
 */
@Data
@TableName("tag_relation")
public class TagRelation {
    /**
     * 关联ID
     */
    @TableId(value = "relation_id", type = IdType.AUTO)
    private Long relationId;

    /**
     * 标签ID
     */
    @TableField("tag_id")
    private Long tagId;

    /**
     * 关联对象类型
     * question/resource/task
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 关联对象ID
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
