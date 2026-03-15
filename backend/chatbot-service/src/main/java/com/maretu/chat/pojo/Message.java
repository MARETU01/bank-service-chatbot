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
 * @since 2026-03-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("message")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话 ID
     */
    private Long sessionId;

    /**
     * 发送者类型：1-用户，2-ai
     */
    private Integer senderType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：TEXT-文本，IMAGE-图片，FILE-文件
     */
    private String messageType;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;


}
