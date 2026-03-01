package com.maretu.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatClient chatClient;

    @RequestMapping(value = "/ai", produces = "text/html;charset=utf-8")
    public Flux<String> chat() {
        return chatClient.prompt()
                .user("今天天气怎么样？")
                .stream()
                .content();
    }
}
