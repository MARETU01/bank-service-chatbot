package com.maretu.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maretu.chat.pojo.Message;
import com.maretu.chat.pojo.Session;
import com.maretu.chat.service.IKnowledgeService;
import com.maretu.chat.service.IMessageService;
import com.maretu.chat.service.ISessionService;
import com.maretu.common.dto.Context;
import com.maretu.common.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ISessionService sessionService;
    private final IMessageService messageService;
    private final IKnowledgeService knowledgeService;
    private final ObjectMapper jacksonObjectMapper;

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

    @PostMapping("/knowledge")
    public Result<List<Map<String, Object>>> uploadDocuments(@RequestHeader("user-info") String userJson,
                                                             @RequestParam("files") MultipartFile[] files) {
        try {
            return Result.success(knowledgeService.uploadDocuments(userJson, files));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @DeleteMapping("/knowledge")
    public Result<Void> clearKnowledge(@RequestHeader("user-info") String userJson) {
        try {
            knowledgeService.clearKnowledge(userJson);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure("清空知识库失败：" + e.getMessage());
        }
    }
}
