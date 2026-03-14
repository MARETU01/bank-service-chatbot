package com.maretu.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDTO {

    /**
     * 会话 ID
     */
    private Long sessionId;

    /**
     * 消息 ID
     */
    private Long messageId;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 时间戳
     */
    private String timestamp;
}
