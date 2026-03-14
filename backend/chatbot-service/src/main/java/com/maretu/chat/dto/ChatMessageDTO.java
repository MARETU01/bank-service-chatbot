package com.maretu.chat.dto;

import lombok.Data;

/**
 * 聊天消息请求 DTO
 */
@Data
public class ChatMessageDTO {

    /**
     * 会话 ID（可选，为空则创建新会话）
     */
    private Long sessionId;

    /**
     * 用户消息内容
     */
    private String message;

    /**
     * 会话标题（创建新会话时使用）
     */
    private String title;
}
