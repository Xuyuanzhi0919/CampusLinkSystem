package com.campuslink.dto.reward;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 兑换记录响应 VO
 */
@Data
@Schema(description = "积分兑换记录")
public class RedeemRecordVO {

    @Schema(description = "记录ID")
    private Long recordId;

    @Schema(description = "商品ID")
    private Long itemId;

    @Schema(description = "商品名称")
    private String itemName;

    @Schema(description = "消耗积分")
    private Integer pointsCost;

    @Schema(description = "效果类型")
    private String effectType;

    @Schema(description = "效果数值")
    private Integer effectValue;

    @Schema(description = "状态：0=待发放 1=已发放 2=已失效")
    private Integer status;

    @Schema(description = "兑换时间")
    private LocalDateTime createdAt;
}
