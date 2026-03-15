package com.maretu.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;

import static com.maretu.chat.config.ChatMemoryAdvisorConfig.SESSION_ID_KEY;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ISessionService sessionService;
    private final IMessageService messageService;
    private final ObjectMapper jacksonObjectMapper;
    private final ChatClient chatClient;

    @GetMapping("/session")
    public Result<List<Session>> getSessions(@RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            return Result.success(sessionService.getSessions(context.getUserId()));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PostMapping("/session")
    public Result<Session> createSession(@RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            return Result.success(sessionService.createSession(context.getUserId()));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @DeleteMapping("/session/{sessionId}")
    public Result<Boolean> deleteSession(@PathVariable String sessionId,
                                      @RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            return Result.success(sessionService.deleteSession(context.getUserId(), sessionId));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PutMapping("/session/{sessionId}")
    public Result<Boolean> renameSession(@RequestHeader("user-info") String userJson,
                                         @PathVariable String sessionId,
                                         @RequestBody Session session) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            return Result.success(sessionService.renameSession(context.getUserId(), sessionId, session));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @GetMapping("/message/{sessionId}")
    public Result<List<Message>> getSessionMessages(@PathVariable String sessionId,
                                                    @RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            return Result.success(messageService.getMessages(context.getUserId(), sessionId));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PostMapping(produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestHeader("user-info") String userJson,
                             @RequestBody Message message) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        return messageService.chat(context.getUserId(), message);
    }

    @GetMapping(value = "/ai", produces = "text/html;charset=utf-8")
    public Flux<String> testChat() {
        return chatClient.prompt("你好。你是谁")
                .advisors(a -> a.param(SESSION_ID_KEY, "123456"))
                .stream()
                .content();
    }
}
