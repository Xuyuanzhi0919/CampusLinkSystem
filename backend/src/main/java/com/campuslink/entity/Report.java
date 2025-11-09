package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报实体类
 */
@Data
@TableName("report")
public class Report {
    /**
     * 举报ID
     */
    @TableId(value = "report_id", type = IdType.AUTO)
    private Long reportId;

    /**
     * 举报者ID
     */
    @TableField("reporter_id")
    private Long reporterId;

    /**
     * 举报类型：1-帖子，2-评论，3-用户，4-活动
     */
    @TableField("report_type")
    private Integer reportType;

    /**
     * 被举报对象ID
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 举报原因类型：1-垃圾信息，2-违规内容，3-骚扰辱骂，4-侵权，5-其他
     */
    @TableField("reason_type")
    private Integer reasonType;

    /**
     * 举报详细描述
     */
    private String description;

    /**
     * 举报凭证图片(多张用逗号分隔)
     */
    @TableField("evidence_images")
    private String evidenceImages;

    /**
     * 处理状态：0-待处理，1-已处理，2-已驳回
     */
    private Integer status;

    /**
     * 处理结果说明
     */
    @TableField("handle_result")
    private String handleResult;

    /**
     * 处理人ID
     */
    @TableField("handler_id")
    private Long handlerId;

    /**
     * 处理时间
     */
    @TableField("handled_at")
    private LocalDateTime handledAt;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
