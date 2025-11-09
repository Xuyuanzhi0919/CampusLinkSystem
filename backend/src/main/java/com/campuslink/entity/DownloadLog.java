package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 下载记录实体类
 */
@Data
@TableName("download_log")
public class DownloadLog {

    /**
     * 下载记录ID
     */
    @TableId(value = "dl_id", type = IdType.AUTO)
    private Long dlId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 资源ID
     */
    @TableField("resource_id")
    private Long resourceId;

    /**
     * 消耗积分
     */
    @TableField("points_cost")
    private Integer pointsCost;

    /**
     * 下载时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
