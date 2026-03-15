package com.maretu.chat.config;

import com.maretu.chat.pojo.Message;
import com.maretu.chat.service.IMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ChatMemoryAdvisorConfig implements BaseChatMemoryAdvisor {

    private final Executor virtualThreadPoolExecutor;

    @Lazy
    @Autowired
    private IMessageService messageService;

    public static final String SESSION_ID_KEY = "sessionId";
    private static final Integer MAX_MEMORY_SIZE = 10;

    @NotNull
    @Override
    public ChatClientRequest before(@NotNull ChatClientRequest chatClientRequest,
                                    @NotNull AdvisorChain advisorChain) {
        String sessionId = chatClientRequest.context().get(SESSION_ID_KEY).toString();
        String userContent = chatClientRequest.prompt().getInstructions().getFirst().getText();

        List<Message> recentMessages = messageService.getRecentMessages(sessionId, MAX_MEMORY_SIZE);

        List<org.springframework.ai.chat.messages.Message> contextMessages = new ArrayList<>();
        
        List<Message> reversedMessages = new ArrayList<>(recentMessages).reversed();

        for (Message historyMsg : reversedMessages) {
            if (historyMsg.getSenderType() == 1) {
                contextMessages.add(new UserMessage(historyMsg.getContent()));
            } else {
                contextMessages.add(new AssistantMessage(historyMsg.getContent()));
            }
        }

        contextMessages.add(new UserMessage(userContent));

        Prompt newPrompt = new Prompt(contextMessages);
        chatClientRequest = new ChatClientRequest(newPrompt, chatClientRequest.context());

        virtualThreadPoolExecutor.execute(() -> {
            Message userMessage = new Message()
                    .setMessageType("TEXT")
                    .setSenderType(1)
                    .setSessionId(sessionId)
                    .setContent(userContent);
            messageService.saveMessage(userMessage);
        });

        System.out.println("实际发送给模型的提示语：" + chatClientRequest.prompt().getInstructions());

        return chatClientRequest;
    }

    @NotNull
    @Override
    public ChatClientResponse after(@NotNull ChatClientResponse chatClientResponse,
                                    @NotNull AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
