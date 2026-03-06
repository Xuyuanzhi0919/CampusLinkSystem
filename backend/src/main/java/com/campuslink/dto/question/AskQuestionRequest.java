package com.campuslink.dto.question;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 提问请求
 */
@Data
public class AskQuestionRequest {
    /**
     * 问题标题
     */
    @NotBlank(message = "问题标题不能为空")
    @Size(min = 5, max = 100, message = "标题长度必须在5-100个字符之间")
    private String title;

    /**
     * 问题内容
     */
    @NotBlank(message = "问题内容不能为空")
    @Size(min = 10, max = 5000, message = "内容长度必须在10-5000个字符之间")
    private String content;

    /**
     * 问题分类
     */
    @NotBlank(message = "问题分类不能为空")
    private String category;

    /**
     * 标签列表 (可选)
     */
    private List<String> tags;

    /**
     * 图片URL列表 (可选)
     */
    private List<String> images;

    /**
     * 悬赏积分 (可选,默认0)
     */
    @Min(value = 0, message = "悬赏积分不能为负数")
    private Integer bounty;
}
