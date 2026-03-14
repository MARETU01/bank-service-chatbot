package com.maretu.chat.service.impl;

import com.maretu.chat.pojo.ChatSessions;
import com.maretu.chat.mapper.ChatSessionsMapper;
import com.maretu.chat.service.IChatSessionsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 聊天会话表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-03-14
 */
@Service
public class ChatSessionsServiceImpl extends ServiceImpl<ChatSessionsMapper, ChatSessions> implements IChatSessionsService {

}
