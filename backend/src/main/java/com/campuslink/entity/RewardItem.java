package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分商品实体
 */
@Data
@TableName("reward_items")
public class RewardItem {

    @TableId(value = "item_id", type = IdType.AUTO)
    private Long itemId;

    /** 商品名称 */
    private String name;

    /** 商品描述 */
    private String description;

    /** 封面图片URL */
    @TableField("cover_img")
    private String coverImg;

    /** 所需积分 */
    @TableField("points_cost")
    private Integer pointsCost;

    /** 库存，-1 表示无限 */
    private Integer stock;

    /** 分类：download | privilege | badge | coupon */
    private String category;

    /** 效果类型，对应后端发放逻辑 */
    @TableField("effect_type")
    private String effectType;

    /** 效果数值（如下载次数、天数等） */
    @TableField("effect_value")
    private Integer effectValue;

    /** 状态：0=下架 1=上架 */
    private Integer status;

    /** 排序权重，越小越靠前 */
    private Integer sort;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
