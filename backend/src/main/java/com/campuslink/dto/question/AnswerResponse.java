package com.campuslink.dto.question;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 答案响应
 */
@Data
public class AnswerResponse {
    /**
     * 答案ID
     */
    private Long aid;

    /**
     * 问题ID
     */
    private Long questionId;

    /**
     * 回答者ID
     */
    private Long answererId;

    /**
     * 回答者昵称
     */
    private String answererNickname;

    /**
     * 回答者头像
     */
    private String answererAvatar;

    /**
     * 答案内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likes;

    /**
     * 是否被采纳
     */
    private Integer isAccepted;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
