package com.maretu.chat.service;

import com.maretu.chat.pojo.Message;
import com.maretu.chat.pojo.Session;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * 聊天服务接口
 */
public interface IChatService {

    Flux<String> chat(Long sessionId, String message, Integer userId);

    Message saveBotMessage(Long sessionId, String content);

    List<Session> getSessions(Integer userId);

    Session createSession(Integer userId, String title);

    void deleteSession(Long sessionId);

    List<Message> getMessages(Long sessionId);
}
