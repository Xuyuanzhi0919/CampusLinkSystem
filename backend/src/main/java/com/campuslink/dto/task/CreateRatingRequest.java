package com.campuslink.dto.task;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 创建任务评价请求
 */
@Data
public class CreateRatingRequest {
    /**
     * 评分(1-5)
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer rating;

    /**
     * 评价内容
     */
    @Size(min = 10, max = 500, message = "评价内容长度必须在10-500字符之间")
    private String comment;

    /**
     * 评价标签
     */
    private List<String> tags;
}
