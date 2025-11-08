package com.campuslink.dto.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 回答问题请求
 */
@Data
public class AnswerQuestionRequest {
    /**
     * 答案内容
     */
    @NotBlank(message = "答案内容不能为空")
    @Size(min = 10, max = 5000, message = "内容长度必须在10-5000个字符之间")
    private String content;
}
