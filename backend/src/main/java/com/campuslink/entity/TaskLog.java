package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务操作日志实体
 */
@Data
@TableName("task_log")
public class TaskLog {
    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long logId;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 操作类型
     */
    private String action;

    /**
     * 操作前状态
     */
    private Integer oldStatus;

    /**
     * 操作后状态
     */
    private Integer newStatus;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 操作时间
     */
    private LocalDateTime createdAt;
}
