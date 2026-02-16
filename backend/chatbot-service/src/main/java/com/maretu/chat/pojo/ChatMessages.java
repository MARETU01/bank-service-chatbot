package com.maretu.chat.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 聊天消息表
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_messages")
public class ChatMessages implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 消息唯一标识
     */
    private String messageId;

    /**
     * 发送者类型: 1-用户, 2-客服, 3-系统
     */
    private Integer senderType;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者名称
     */
    private String senderName;

    /**
     * 消息类型: TEXT-文本, IMAGE-图片, FILE-文件, QUICK_REPLY-快捷回复
     */
    private String messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 阅读时间
     */
    private LocalDateTime readAt;

    /**
     * 额外数据(图片URL、文件信息等)
     */
    private String extraData;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;


}
