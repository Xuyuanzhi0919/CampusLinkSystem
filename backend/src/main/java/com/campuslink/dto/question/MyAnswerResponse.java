package com.campuslink.dto.question;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 「我的回答」列表响应（AnswerResponse + 所回答的问题摘要）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MyAnswerResponse extends AnswerResponse {

    /**
     * 所回答的问题（包含 qid 和 title）
     */
    private QuestionBrief question;

    @Data
    public static class QuestionBrief {
        private Long qid;
        private String title;
    }
}
