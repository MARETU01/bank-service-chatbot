package com.maretu.chat.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
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
    private OpenAiEmbeddingModel openAiEmbeddingModel;

    @Autowired
    private FunctionCallTools functionCallTools;

    @Bean
    @Primary
    public ChatClient chatClient() {
        ChatModel primaryModel = openAiChatModel != null ? openAiChatModel : ollamaChatModel;
        if (primaryModel == null) {
            throw new IllegalStateException("No chat model available. Please configure OpenAI or Ollama.");
        }
        return ChatClient.builder(primaryModel)
                .defaultSystem("你是一个银行智能客服助手，可以使用function call帮助用户查询账户信息")
                .defaultTools(functionCallTools)
                .build();
    }

    @Bean(name = "openAiChatClient")
    public ChatClient openAiChatClient() {
        if (openAiChatModel == null) {
            return null;
        }
        return ChatClient.builder(openAiChatModel)
                .build();
    }

    @Bean(name = "ollamaChatClient")
    public ChatClient ollamaChatClient() {
        if (ollamaChatModel == null) {
            return null;
        }
        return ChatClient.builder(ollamaChatModel)
                .build();
    }

    @Bean
    public VectorStore vectorStore() {
        return SimpleVectorStore.builder(openAiEmbeddingModel).build();
    }
}
