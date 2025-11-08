package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学校实体类
 */
@Data
@TableName("school")
public class School {

    /**
     * 学校ID
     */
    @TableId(type = IdType.AUTO)
    private Long schoolId;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 所在省份
     */
    private String province;

    /**
     * 所在城市
     */
    private String city;

    /**
     * 学校地址
     */
    private String address;

    /**
     * 学校Logo URL
     */
    @TableField("logo_url")
    private String logoUrl;

    /**
     * 状态：0-禁用，1-正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
