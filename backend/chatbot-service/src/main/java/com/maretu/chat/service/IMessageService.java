package com.maretu.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.maretu.chat.pojo.Message;
import com.baomidou.mybatisplus.extension.service.IService;
import com.maretu.common.dto.ChatStatsDTO;
import com.maretu.common.dto.Context;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * <p>
 * 聊天消息表 服务类
 * </p>
 *
 * @author maretu
 * @since 2026-03-14
 */
public interface IMessageService extends IService<Message> {

    List<Message> getMessages(Integer userId, String sessionId);

    Flux<String> chat(String userJson, Message message) throws JsonProcessingException;

    List<org.springframework.ai.chat.messages.Message> getRecentMessages(String sessionId, Integer limit);

    ChatStatsDTO getChatStats(String userJson);
}
