package com.maretu.chat.service.impl;

import com.maretu.chat.pojo.Message;
import com.maretu.chat.mapper.MessageMapper;
import com.maretu.chat.service.IMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maretu.chat.service.ISessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;


/**
 * <p>
 * 聊天消息表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-03-14
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

    private final ISessionService sessionService;
    private final ChatClient chatClient;
    private final Executor virtualThreadPoolExecutor;

    @Override
    public List<Message> getMessages(Integer userId, String sessionId) {
        if (!sessionService.isSessionOwner(userId, sessionId)) {
            throw new RuntimeException("无权访问该会话的消息");
        }
        return lambdaQuery()
                .eq(Message::getSessionId, sessionId)
                .orderByAsc(Message::getCreatedAt)
                .list();
    }

    @Override
    public Flux<String> chat(Integer userId, Message message) {
        if (!sessionService.isSessionOwner(userId, message.getSessionId())) {
            throw new RuntimeException("无权访问该会话的消息");
        }
        StringBuilder fullResponse = new StringBuilder();
        return chatClient.prompt()
                .user(message.getContent())
                .messages(getRecentMessages(message.getSessionId(), 20))
                .stream()
                .content()
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> virtualThreadPoolExecutor.execute(() -> save(new Message()
                        .setMessageType("TEXT")
                        .setSenderType(2)
                        .setSessionId(message.getSessionId())
                        .setContent(fullResponse.toString()))));
    }

    @Override
    public List<org.springframework.ai.chat.messages.Message> getRecentMessages(String sessionId, Integer limit) {
        List<Message> dbMessages = lambdaQuery()
                .eq(Message::getSessionId, sessionId)
                .orderByDesc(Message::getCreatedAt)
                .last("LIMIT " + limit)
                .list()
                .reversed();
        List<org.springframework.ai.chat.messages.Message> contextMessages = new ArrayList<>();
        for (Message message : dbMessages) {
            if (message.getSenderType() == 1) {
                contextMessages.add(new UserMessage(message.getContent()));
            } else if (message.getSenderType() == 2) {
                contextMessages.add(new AssistantMessage(message.getContent()));
            }
        }
        return contextMessages;
    }
}
