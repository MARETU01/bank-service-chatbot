package com.maretu.chat.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maretu.chat.pojo.Message;
import com.maretu.chat.mapper.MessageMapper;
import com.maretu.chat.service.IMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maretu.chat.service.ISessionService;
import com.maretu.common.dto.Context;
import com.maretu.common.utils.ChatGuardUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;


/**
 * <p>
 * 聊天消息表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-03-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

    private final ObjectMapper jacksonObjectMapper;
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
    public Flux<String> chat(String userJson, Message message) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        if (!sessionService.isSessionOwner(context.getUserId(), message.getSessionId())) {
            throw new RuntimeException("无权访问该会话的消息");
        }

        // ===== 输入安全防护 =====
        ChatGuardUtils.GuardResult guardResult = ChatGuardUtils.processInput(message.getContent());
        if (!guardResult.isPassed()) {
            // 输入未通过安全检查，直接返回拒绝原因，同时保存对话记录
            String rejectReason = guardResult.getRejectReason();
            log.warn("用户[{}]输入未通过安全检查: {}", context.getUserId(), rejectReason);
            virtualThreadPoolExecutor.execute(() -> {
                List<Message> saveMessages = new ArrayList<>();
                saveMessages.add(message.setSenderType(1).setMessageType("TEXT"));
                saveMessages.add(new Message()
                        .setMessageType("TEXT")
                        .setSenderType(2)
                        .setSessionId(message.getSessionId())
                        .setContent(rejectReason));
                saveBatch(saveMessages, 2);
            });
            return Flux.just(rejectReason);
        }
        // 使用安全处理后的内容（已脱敏、已清理）
        String safeContent = guardResult.getSafeContent();

        // ===== 调用 LLM =====
        StringBuilder fullResponse = new StringBuilder();
        return chatClient.prompt()
                .user(safeContent)
                .messages(getRecentMessages(message.getSessionId(), 20))
                .toolContext(Map.of("context", userJson))
                .stream()
                .content()
                // ===== 输出安全审核（逐片段累积后在完成时审核） =====
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> virtualThreadPoolExecutor.execute(() -> {
                    // 对完整回答进行输出审核
                    String reviewedResponse = ChatGuardUtils.reviewOutput(fullResponse.toString());
                    if (!reviewedResponse.equals(fullResponse.toString())) {
                        log.warn("用户[{}]的AI回答未通过输出审核，已替换为安全回答", context.getUserId());
                    }
                    List<Message> saveMessages = new ArrayList<>();
                    saveMessages.add(message.setSenderType(1).setMessageType("TEXT"));
                    saveMessages.add(new Message()
                            .setMessageType("TEXT")
                            .setSenderType(2)
                            .setSessionId(message.getSessionId())
                            .setContent(reviewedResponse));
                    saveBatch(saveMessages, 2);
                }));
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
