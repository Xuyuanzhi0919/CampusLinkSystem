package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源实体类
 */
@Data
@TableName("resource")
public class Resource {
    @TableId(value = "r_id", type = IdType.AUTO)
    private Long rid;

    private String title;

    private String description;

    @TableField("uploader_id")
    private Long uploaderId;

    @TableField("file_url")
    private String fileUrl;

    @TableField("file_name")
    private String fileName;

    @TableField("file_size")
    private Long fileSize;

    @TableField("file_type")
    private String fileType;

    private String category;

    @TableField("course_name")
    private String courseName;

    @TableField("school_id")
    private Long schoolId;

    private Integer score;

    private Integer downloads;

    private Integer likes;

    /**
     * 状态：0-待审核，1-已通过，2-已拒绝
     */
    private Integer status;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /** 上传者昵称（非DB字段，管理端查询时填充） */
    @TableField(exist = false)
    private String uploaderName;
}
