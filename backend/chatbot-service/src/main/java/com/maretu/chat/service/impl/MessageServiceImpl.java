package com.maretu.chat.service.impl;

import com.maretu.chat.pojo.Message;
import com.maretu.chat.mapper.MessageMapper;
import com.maretu.chat.service.IMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maretu.chat.service.ISessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

import static com.maretu.chat.config.ChatMemoryAdvisorConfig.SESSION_ID_KEY;

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
        return chatClient.prompt(message.getContent())
                .advisors(a -> a.param(SESSION_ID_KEY, message.getSessionId()))
                .stream()
                .content();
    }

    @Override
    public List<Message> getRecentMessages(String sessionId, Integer limit) {
        return lambdaQuery()
                .eq(Message::getSessionId, sessionId)
                .orderByDesc(Message::getCreatedAt)
                .last("LIMIT " + limit)
                .list();
    }

    @Override
    @Async("virtualThreadPoolExecutor")
    public void saveMessage(Message message) {
        save(message);
    }
}
