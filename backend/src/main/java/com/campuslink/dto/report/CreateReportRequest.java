package com.campuslink.dto.report;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建举报请求
 */
@Data
public class CreateReportRequest {
    /**
     * 举报类型：1-帖子，2-评论，3-用户，4-活动
     */
    @NotNull(message = "举报类型不能为空")
    @Min(value = 1, message = "举报类型无效")
    @Max(value = 4, message = "举报类型无效")
    private Integer reportType;

    /**
     * 被举报对象ID
     */
    @NotNull(message = "被举报对象ID不能为空")
    private Long targetId;

    /**
     * 举报原因类型：1-垃圾信息，2-违规内容，3-骚扰辱骂，4-侵权，5-其他
     */
    @NotNull(message = "举报原因不能为空")
    @Min(value = 1, message = "举报原因类型无效")
    @Max(value = 5, message = "举报原因类型无效")
    private Integer reasonType;

    /**
     * 举报详细描述
     */
    @NotBlank(message = "举报描述不能为空")
    private String description;

    /**
     * 举报凭证图片(多张用逗号分隔)
     */
    private String evidenceImages;
}
