package com.maretu.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maretu.chat.pojo.Message;
import com.maretu.chat.pojo.Session;
import com.maretu.chat.service.IChatService;
import com.maretu.chat.service.IMessageService;
import com.maretu.chat.service.ISessionService;
import com.maretu.common.dto.Context;
import com.maretu.common.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

import static com.maretu.chat.config.ChatMemoryAdvisorConfig.SESSION_ID_KEY;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final IChatService chatService;
    private final ISessionService sessionService;
    private final IMessageService messageService;
    private final ObjectMapper jacksonObjectMapper;
    private final ChatClient chatClient;

    @GetMapping("/sessions")
    public Result<List<Session>> getSessions(@RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            return Result.success(sessionService.getSessions(context.getUserId()));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @GetMapping("/session/{sessionId}")
    public Result<List<Message>> getSessionMessages(@PathVariable String sessionId,
                                                    @RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            return Result.success(messageService.getMessages(context.getUserId(), sessionId));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @GetMapping(value = "/ai", produces = "text/html;charset=utf-8")
    public Flux<String> testChat() {
        return chatClient.prompt("你好。你是谁")
                .advisors(a -> a.param(SESSION_ID_KEY, "123456"))
                .stream()
                .content();
    }
}
