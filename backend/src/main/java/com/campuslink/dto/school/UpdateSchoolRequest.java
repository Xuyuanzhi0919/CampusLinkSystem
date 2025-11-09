package com.campuslink.dto.school;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新学校请求DTO
 */
@Data
public class UpdateSchoolRequest {

    @Size(max = 200, message = "学校名称最多200个字符")
    private String schoolName;

    @Size(max = 50, message = "省份最多50个字符")
    private String province;

    @Size(max = 50, message = "城市最多50个字符")
    private String city;

    @Size(max = 500, message = "地址最多500个字符")
    private String address;

    @Size(max = 500, message = "Logo URL最多500个字符")
    private String logoUrl;

    private Integer status;
}
