package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分兑换记录实体
 */
@Data
@TableName("redeem_records")
public class RedeemRecord {

    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    @TableField("user_id")
    private Long userId;

    @TableField("item_id")
    private Long itemId;

    /** 商品名称快照（防止商品下架后丢失信息） */
    @TableField("item_name")
    private String itemName;

    /** 消耗积分快照 */
    @TableField("points_cost")
    private Integer pointsCost;

    /** 效果类型快照，用于权益校验 */
    @TableField("effect_type")
    private String effectType;

    /** 效果数值快照 */
    @TableField("effect_value")
    private Integer effectValue;

    /** 状态：0=待发放 1=已发放 2=已失效 */
    private Integer status;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
