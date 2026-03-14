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
    /** 状态：0-已解散，1-正常，2-待审核，3-已拒绝 */
    private Integer status;
    /** 拒绝原因（status=3 时有值） */
    private String rejectReason;
    /** 是否官方：0-非官方，1-官方 */
    private Integer isOfficial;
    private String category;
    private LocalDateTime createdAt;
}
