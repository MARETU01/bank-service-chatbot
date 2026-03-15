package com.maretu.chat.service.impl;

import com.maretu.chat.pojo.Message;
import com.maretu.chat.mapper.MessageMapper;
import com.maretu.chat.service.IMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 聊天消息表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-03-14
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

    @Override
    @Async("virtualThreadPoolExecutor")
    public void saveMessage(Message message) {
        save(message);
    }
}
