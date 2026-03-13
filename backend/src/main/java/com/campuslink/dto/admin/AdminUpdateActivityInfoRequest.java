package com.campuslink.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUpdateActivityInfoRequest {

    @Size(max = 100, message = "地点不超过100字")
    private String location;

    @Min(value = 1, message = "最大人数至少为1")
    private Integer maxParticipants;

    @Min(value = 0, message = "积分不能为负")
    private Integer rewardPoints;
}
