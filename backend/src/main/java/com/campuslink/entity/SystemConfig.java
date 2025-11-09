package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 */
@Data
@TableName("system_config")
public class SystemConfig {

    @TableId(value = "config_id", type = IdType.AUTO)
    private Long configId;

    private String configKey;

    private String configValue;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
