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
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ISessionService sessionService;
    private final IMessageService messageService;
    private final ObjectMapper jacksonObjectMapper;
    private final OpenAiEmbeddingModel openAiEmbeddingModel;

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
    public Result<Boolean> deleteSession(@PathVariable("sessionId") String sessionId,
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
                                         @PathVariable("sessionId") String sessionId,
                                         @RequestBody Session session) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            return Result.success(sessionService.renameSession(context.getUserId(), sessionId, session));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @GetMapping("/message/{sessionId}")
    public Result<List<Message>> getSessionMessages(@PathVariable("sessionId") String sessionId,
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
        return messageService.chat(userJson, message);
    }

    @GetMapping("/test")
    public Result<Void> test() {
        float[] helloWorlds = openAiEmbeddingModel.embed("hello world");
        System.out.println("hello world embedding: " + Arrays.toString(helloWorlds));
        return Result.success(null);
    }
}
