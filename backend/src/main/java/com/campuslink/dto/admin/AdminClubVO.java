package com.campuslink.dto.admin;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理端社团视图对象
 */
@Data
public class AdminClubVO {
    private Long clubId;
    private String clubName;
    private String description;
    private String logoUrl;
    private Long founderId;
    private String founderName;
    private Integer memberCount;
    /** 状态：0-禁用，1-正常 */
    private Integer status;
    /** 是否官方：0-非官方，1-官方 */
    private Integer isOfficial;
    private String category;
    private LocalDateTime createdAt;
}
