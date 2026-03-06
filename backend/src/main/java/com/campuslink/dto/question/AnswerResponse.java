package com.campuslink.dto.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 答案响应
 */
@Data
public class AnswerResponse {
    /**
     * 答案ID (前端使用answerId)
     */
    @JsonProperty("answerId")
    private Long aid;

    /**
     * 问题ID
     */
    private Long questionId;

    /**
     * 回答者ID (前端使用responderId)
     */
    @JsonProperty("responderId")
    private Long answererId;

    /**
     * 回答者昵称 (前端使用responderName)
     */
    @JsonProperty("responderName")
    private String answererNickname;

    /**
     * 回答者头像 (前端使用responderAvatar)
     */
    @JsonProperty("responderAvatar")
    private String answererAvatar;

    /**
     * 答案内容
     */
    private String content;

    /**
     * 图片URL列表
     */
    private List<String> images;

    /**
     * 点赞数
     */
    private Integer likes;

    /**
     * 是否被采纳 (转换为boolean)
     */
    private Boolean isAccepted;

    /**
     * 是否已点赞 (默认false,需要业务层填充)
     */
    private Boolean isLiked = false;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
