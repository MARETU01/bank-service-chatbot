package com.maretu.chat.config;

import com.maretu.chat.service.IChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * AI 配置类
 * 支持多模型配置，OpenAI 失败时可降级到 Ollama
 */
@Configuration
public class AiConfig {

    @Autowired(required = false)
    private OpenAiChatModel openAiChatModel;

    @Autowired(required = false)
    private OllamaChatModel ollamaChatModel;

    @Autowired
    private IChatService chatService;

    @Bean
    @Primary
    public ChatClient chatClient() {
        ChatModel primaryModel = openAiChatModel != null ? openAiChatModel : ollamaChatModel;
        if (primaryModel == null) {
            throw new IllegalStateException("No chat model available. Please configure OpenAI or Ollama.");
        }
        return ChatClient.builder(primaryModel)
                .defaultAdvisors(chatService)
                .build();
    }

    @Bean(name = "openAiChatClient")
    public ChatClient openAiChatClient() {
        if (openAiChatModel == null) {
            return null;
        }
        return ChatClient.builder(openAiChatModel).build();
    }

    /**
     * Ollama ChatClient（作为备用）
     */
    @Bean(name = "ollamaChatClient")
    public ChatClient ollamaChatClient() {
        if (ollamaChatModel == null) {
            return null;
        }
        return ChatClient.builder(ollamaChatModel).build();
    }
}