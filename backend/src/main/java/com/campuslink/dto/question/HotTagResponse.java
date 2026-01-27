package com.campuslink.dto.question;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热门标签响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "热门标签响应")
public class HotTagResponse {
    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "使用次数")
    private Integer count;
}
