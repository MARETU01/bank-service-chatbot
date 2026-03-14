package com.maretu.chat.service.impl;

import com.maretu.chat.service.IChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.stereotype.Service;

/**
 * 聊天服务实现类
 */
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements IChatService {

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        System.out.println("ChatServiceImpl.before: " + chatClientRequest);
        System.out.println("before logic executed");
        System.out.println("AdvisorChain: " + advisorChain);
        return null;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        System.out.println("ChatServiceImpl.before: " + chatClientResponse);
        System.out.println("after logic executed");
        System.out.println("AdvisorChain: " + advisorChain);
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
