package com.maretu.chat.service.impl;

import com.maretu.chat.pojo.Message;
import com.maretu.chat.mapper.MessageMapper;
import com.maretu.chat.service.IMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 聊天消息表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-03-14
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

    @Override
    public List<Message> getMessagesBySessionId(Long sessionId) {
        return lambdaQuery()
                .eq(Message::getSessionId, sessionId)
                .orderByAsc(Message::getCreatedAt)
                .list();
    }

    @Override
    public Message saveUserMessage(Long sessionId, String content) {
        Message message = new Message();
        message.setSessionId(sessionId);
        message.setSenderType(1); // 1-用户
        message.setContent(content);
        message.setMessageType("TEXT");
        message.setCreatedAt(LocalDateTime.now());
        save(message);
        return message;
    }

    @Override
    public Message saveBotMessage(Long sessionId, String content) {
        Message message = new Message();
        message.setSessionId(sessionId);
        message.setSenderType(2); // 2-客服/AI
        message.setContent(content);
        message.setMessageType("TEXT");
        message.setCreatedAt(LocalDateTime.now());
        save(message);
        return message;
    }
}
