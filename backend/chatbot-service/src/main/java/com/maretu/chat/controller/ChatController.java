package com.maretu.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maretu.chat.dto.ChatMessageDTO;
import com.maretu.chat.pojo.Message;
import com.maretu.chat.pojo.Session;
import com.maretu.chat.service.IMessageService;
import com.maretu.chat.service.ISessionService;
import com.maretu.common.dto.Context;
import com.maretu.common.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatClient chatClient;
    private final ObjectMapper jacksonObjectMapper;
    private final ISessionService sessionService;
    private final IMessageService messageService;

    /**
     * 流式聊天接口
     * 用户发送消息，AI 流式返回回复
     */
    @PostMapping(value = "/message", produces = "text/event-stream;charset=utf-8")
    public Flux<String> chat(@RequestBody ChatMessageDTO chatMessageDTO,
                             @RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        Integer userId = context.getUserId();

        // 如果没有会话 ID，创建新会话
        Long sessionId = chatMessageDTO.getSessionId();
        if (sessionId == null) {
            Session session = sessionService.createSession(userId, chatMessageDTO.getTitle());
            sessionId = session.getId();
        }

        // 保存用户消息
        messageService.saveUserMessage(sessionId, chatMessageDTO.getMessage());

        // 流式返回 AI 回复
        return chatClient.prompt()
                .user(chatMessageDTO.getMessage())
                .stream()
                .content()
                .doOnComplete(() -> {
                    // 注意：由于是流式响应，完整的回复内容需要在客户端收集后单独调用保存接口
                    // 或者使用 block() 收集完整响应后保存，但这样会失去流式效果
                    // 这里采用折中方案：在客户端收集完整响应后调用另一个接口保存
                });
    }

    /**
     * 保存 AI 回复消息（流式响应完成后调用）
     */
    @PostMapping("/message/save")
    public Result<Message> saveBotMessage(@RequestBody Map<String, Object> params) {
        try {
            Long sessionId = Long.valueOf(params.get("sessionId").toString());
            String content = params.get("content").toString();
            Message message = messageService.saveBotMessage(sessionId, content);
            return Result.success(message);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 获取指定会话的聊天记录
     */
    @GetMapping("/messages/{sessionId}")
    public Result<List<Message>> getMessages(@PathVariable Long sessionId) {
        try {
            List<Message> messages = messageService.getMessagesBySessionId(sessionId);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 获取用户的聊天会话列表
     */
    @GetMapping("/sessions")
    public Result<List<Session>> getChatSessions(@RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            List<Session> sessions = sessionService.getSessions(context.getUserId());
            return Result.success(sessions);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 创建新的聊天会话
     */
    @PostMapping("/sessions")
    public Result<Session> createSession(@RequestHeader("user-info") String userJson,
                                         @RequestBody Map<String, String> params) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            String title = params.get("title");
            Session session = sessionService.createSession(context.getUserId(), title);
            return Result.success(session);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 删除聊天会话（软删除）
     */
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(@PathVariable Long sessionId) {
        try {
            sessionService.deleteSession(sessionId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 测试接口 - 固定问题
     */
    @RequestMapping(value = "/ai", produces = "text/html;charset=utf-8")
    public Flux<String> testChat() {
        return chatClient.prompt()
                .user("今天天气怎么样？")
                .stream()
                .content();
    }
}
