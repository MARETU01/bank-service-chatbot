package com.maretu.chat.service.impl;

import com.maretu.chat.pojo.ChatMessages;
import com.maretu.chat.mapper.ChatMessagesMapper;
import com.maretu.chat.service.IChatMessagesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 聊天消息表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Service
public class ChatMessagesServiceImpl extends ServiceImpl<ChatMessagesMapper, ChatMessages> implements IChatMessagesService {

}
