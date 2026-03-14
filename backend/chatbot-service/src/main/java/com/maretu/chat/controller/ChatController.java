package com.maretu.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maretu.chat.pojo.Session;
import com.maretu.chat.service.ISessionService;
import com.maretu.common.dto.Context;
import com.maretu.common.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatClient chatClient;
    private final ObjectMapper jacksonObjectMapper;
    private final ISessionService sessionService;

    @RequestMapping(value = "/ai", produces = "text/html;charset=utf-8")
    public Flux<String> chat() {
        return chatClient.prompt()
                .user("今天天气怎么样？")
                .stream()
                .content();
    }

    @GetMapping("/sessions")
    public Result<List<Session>> getChatSessions(@RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            return Result.success(sessionService.getSessions(context.getUserId()));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}
