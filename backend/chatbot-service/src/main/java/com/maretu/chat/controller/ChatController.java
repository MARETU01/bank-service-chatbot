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
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

import static com.maretu.chat.config.ChatMemoryAdvisorConfig.SESSION_ID_KEY;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final IChatService chatService;
    private final ObjectMapper jacksonObjectMapper;
    private final ChatClient chatClient;

//    @PostMapping(value = "/message", produces = "text/event-stream;charset=utf-8")
//    public Flux<String> chat(@RequestBody ChatMessageDTO dto,
//                             @RequestHeader("user-info") String userJson) throws JsonProcessingException {
//        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
//        return chatService.chat(dto.getSessionId(), dto.getMessage(), context.getUserId());
//    }
//
//    @GetMapping("/messages/{sessionId}")
//    public Result<List<Message>> getMessages(@PathVariable Long sessionId) {
//        try {
//            return Result.success(chatService.getMessages(sessionId));
//        } catch (Exception e) {
//            return Result.failure(e.getMessage());
//        }
//    }
//
//    @GetMapping("/sessions")
//    public Result<List<Session>> getSessions(@RequestHeader("user-info") String userJson) throws JsonProcessingException {
//        try {
//            Context context = jacksonObjectMapper.readValue(userJson, Context.class);
//            return Result.success(chatService.getSessions(context.getUserId()));
//        } catch (Exception e) {
//            return Result.failure(e.getMessage());
//        }
//    }
//
//    @PostMapping("/sessions")
//    public Result<Session> createSession(@RequestHeader("user-info") String userJson,
//                                         @RequestBody Map<String, String> params) throws JsonProcessingException {
//        try {
//            Context context = jacksonObjectMapper.readValue(userJson, Context.class);
//            return Result.success(chatService.createSession(context.getUserId(), params.get("title")));
//        } catch (Exception e) {
//            return Result.failure(e.getMessage());
//        }
//    }
//
//    @DeleteMapping("/sessions/{sessionId}")
//    public Result<Void> deleteSession(@PathVariable Long sessionId) {
//        try {
//            chatService.deleteSession(sessionId);
//            return Result.success(null);
//        } catch (Exception e) {
//            return Result.failure(e.getMessage());
//        }
//    }
//
    @GetMapping(value = "/ai", produces = "text/html;charset=utf-8")
    public Flux<String> testChat() {
        return chatClient.prompt("你好。你是谁")
                .advisors(a -> a.param(SESSION_ID_KEY, "123456"))
                .stream()
                .content();
    }
}
