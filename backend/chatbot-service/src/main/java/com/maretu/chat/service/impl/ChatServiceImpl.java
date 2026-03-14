package com.maretu.chat.service.impl;

import com.maretu.chat.pojo.Message;
import com.maretu.chat.pojo.Session;
import com.maretu.chat.service.IChatService;
import com.maretu.chat.service.IMessageService;
import com.maretu.chat.service.ISessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 聊天服务实现类
 */
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements IChatService {

    private final ChatClient chatClient;
    private final IMessageService messageService;
    private final ISessionService sessionService;

    @Override
    public Flux<String> chat(Long sessionId, String message, Integer userId) {
        // 如果没有会话 ID，创建新会话
        if (sessionId == null) {
            Session session = sessionService.createSession(userId, "新会话");
            sessionId = session.getId();
        }

        // 保存用户消息
        messageService.saveUserMessage(sessionId, message);

        // 流式返回 AI 回复
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }

    @Override
    public Message saveBotMessage(Long sessionId, String content) {
        return messageService.saveBotMessage(sessionId, content);
    }

    @Override
    public List<Session> getSessions(Integer userId) {
        return sessionService.getSessions(userId);
    }

    @Override
    public Session createSession(Integer userId, String title) {
        return sessionService.createSession(userId, title);
    }

    @Override
    public void deleteSession(Long sessionId) {
        sessionService.deleteSession(sessionId);
    }

    @Override
    public List<Message> getMessages(Long sessionId) {
        return messageService.getMessagesBySessionId(sessionId);
    }
}
