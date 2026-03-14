package com.maretu.chat.service;

import com.maretu.chat.pojo.Message;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 聊天消息表 服务类
 * </p>
 *
 * @author maretu
 * @since 2026-03-14
 */
public interface IMessageService extends IService<Message> {

    /**
     * 获取指定会话的所有消息
     * @param sessionId 会话 ID
     * @return 消息列表
     */
    List<Message> getMessagesBySessionId(Long sessionId);

    /**
     * 保存用户消息
     * @param sessionId 会话 ID
     * @param content 消息内容
     * @return 保存后的消息
     */
    Message saveUserMessage(Long sessionId, String content);

    /**
     * 保存机器人消息
     * @param sessionId 会话 ID
     * @param content 消息内容
     * @return 保存后的消息
     */
    Message saveBotMessage(Long sessionId, String content);
}
