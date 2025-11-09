package com.campuslink.dto.report;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报响应
 */
@Data
public class ReportResponse {
    /**
     * 举报ID
     */
    private Long reportId;

    /**
     * 举报者ID
     */
    private Long reporterId;

    /**
     * 举报者昵称
     */
    private String reporterName;

    /**
     * 举报者头像
     */
    private String reporterAvatar;

    /**
     * 举报类型：1-帖子，2-评论，3-用户，4-活动
     */
    private Integer reportType;

    /**
     * 举报类型名称
     */
    private String reportTypeName;

    /**
     * 被举报对象ID
     */
    private Long targetId;

    /**
     * 被举报对象标题/内容摘要
     */
    private String targetContent;

    /**
     * 举报原因类型：1-垃圾信息，2-违规内容，3-骚扰辱骂，4-侵权，5-其他
     */
    private Integer reasonType;

    /**
     * 举报原因类型名称
     */
    private String reasonTypeName;

    /**
     * 举报详细描述
     */
    private String description;

    /**
     * 举报凭证图片(多张用逗号分隔)
     */
    private String evidenceImages;

    /**
     * 处理状态：0-待处理，1-已处理，2-已驳回
     */
    private Integer status;

    /**
     * 处理状态名称
     */
    private String statusName;

    /**
     * 处理结果说明
     */
    private String handleResult;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理人昵称
     */
    private String handlerName;

    /**
     * 处理时间
     */
    private LocalDateTime handledAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
