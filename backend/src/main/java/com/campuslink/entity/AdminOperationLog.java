package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员操作日志
 */
@Data
@TableName("admin_operation_log")
public class AdminOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作管理员 userId */
    private Long operatorId;

    /** 操作类型 */
    private String action;

    /** 操作目标描述（如"用户[uid=5]"） */
    private String target;

    /** 详细说明 */
    private String detail;

    /** 操作时间 */
    private LocalDateTime createdAt;

    /** 操作者昵称（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String operatorName;
}
