package com.campuslink.dto.reward;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 积分商品响应 VO
 */
@Data
@Schema(description = "积分商品")
public class RewardItemVO {

    @Schema(description = "商品ID")
    private Long itemId;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "封面图片URL")
    private String coverImg;

    @Schema(description = "所需积分")
    private Integer pointsCost;

    @Schema(description = "库存，-1 为无限")
    private Integer stock;

    @Schema(description = "分类：download | privilege | badge | coupon")
    private String category;

    @Schema(description = "效果类型")
    private String effectType;

    @Schema(description = "效果数值")
    private Integer effectValue;

    @Schema(description = "排序权重")
    private Integer sort;
}
