package com.maretu.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ChatMemoryAdvisorConfig implements BaseChatMemoryAdvisor {

    @NotNull
    @Override
    public ChatClientRequest before(@NotNull ChatClientRequest chatClientRequest,
                                    @NotNull AdvisorChain advisorChain) {
        log.info("ChatMemoryAdvisor - 处理请求前");
        log.info("请求内容：{}", chatClientRequest);
        chatClientRequest.prompt().getInstructions().getFirst().getText();

        // TODO: 实现对话记忆检索逻辑
        // 例如：从 Redis 或数据库中检索用户的对话历史
        // 并将历史上下文添加到请求中
        
        return chatClientRequest;
    }

    @NotNull
    @Override
    public ChatClientResponse after(@NotNull ChatClientResponse chatClientResponse,
                                    @NotNull AdvisorChain advisorChain) {
        log.info("ChatMemoryAdvisor - 处理响应后");
        log.info("响应内容：{}", chatClientResponse.chatResponse());
        
        // TODO: 实现对话记忆存储逻辑
        // 例如：将当前对话保存到 Redis 或数据库中
        
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
