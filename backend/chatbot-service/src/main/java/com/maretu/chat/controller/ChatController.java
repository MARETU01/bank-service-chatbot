package com.maretu.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maretu.chat.dto.ChatMessageDTO;
import com.maretu.chat.pojo.Message;
import com.maretu.chat.pojo.Session;
import com.maretu.chat.service.IChatService;
import com.maretu.common.dto.Context;
import com.maretu.common.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final IChatService chatService;
    private final ObjectMapper jacksonObjectMapper;

    /**
     * 流式聊天接口
     */
    @PostMapping(value = "/message", produces = "text/event-stream;charset=utf-8")
    public Flux<String> chat(@RequestBody ChatMessageDTO dto,
                             @RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        return chatService.chat(dto.getSessionId(), dto.getMessage(), context.getUserId());
    }

    /**
     * 保存 AI 回复消息
     */
    @PostMapping("/message/save")
    public Result<Message> saveBotMessage(@RequestBody Map<String, Object> params) {
        try {
            Long sessionId = Long.valueOf(params.get("sessionId").toString());
            String content = params.get("content").toString();
            return Result.success(chatService.saveBotMessage(sessionId, content));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 获取聊天记录
     */
    @GetMapping("/messages/{sessionId}")
    public Result<List<Message>> getMessages(@PathVariable Long sessionId) {
        try {
            return Result.success(chatService.getMessages(sessionId));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/sessions")
    public Result<List<Session>> getSessions(@RequestHeader("user-info") String userJson) throws JsonProcessingException {
        try {
            Context context = jacksonObjectMapper.readValue(userJson, Context.class);
            return Result.success(chatService.getSessions(context.getUserId()));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 创建会话
     */
    @PostMapping("/sessions")
    public Result<Session> createSession(@RequestHeader("user-info") String userJson,
                                         @RequestBody Map<String, String> params) throws JsonProcessingException {
        try {
            Context context = jacksonObjectMapper.readValue(userJson, Context.class);
            return Result.success(chatService.createSession(context.getUserId(), params.get("title")));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(@PathVariable Long sessionId) {
        try {
            chatService.deleteSession(sessionId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 测试接口
     */
    @GetMapping("/ai")
    public Flux<String> testChat() {
        return chatService.chat(null, "今天天气怎么样？", 1);
    }
}
