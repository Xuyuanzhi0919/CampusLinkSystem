package com.campuslink.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 私信发送事件
 *
 * 设计要点:
 * 1. 继承 ApplicationEvent,符合 Spring 事件驱动模型
 * 2. 在 MessageService 事务提交后发布,确保通知消费者看到的是已提交数据
 * 3. 包含通知所需的所有信息,避免消费者再查数据库
 */
@Getter
public class MessageSentEvent extends ApplicationEvent {

    /**
     * 消息 ID
     */
    private final Long messageId;

    /**
     * 发送者 ID
     */
    private final Long senderId;

    /**
     * 发送者昵称
     */
    private final String senderNickname;

    /**
     * 接收者 ID
     */
    private final Long receiverId;

    /**
     * 消息类型: 1-文本,2-图片,3-文件
     */
    private final Integer msgType;

    /**
     * 消息内容(文本消息时有值)
     */
    private final String content;

    public MessageSentEvent(Object source, Long messageId, Long senderId, String senderNickname,
                            Long receiverId, Integer msgType, String content) {
        super(source);
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.receiverId = receiverId;
        this.msgType = msgType;
        this.content = content;
    }
}
