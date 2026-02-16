package com.maretu.chat.service.impl;

import com.maretu.chat.pojo.ChatTags;
import com.maretu.chat.mapper.ChatTagsMapper;
import com.maretu.chat.service.IChatTagsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 聊天标签表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Service
public class ChatTagsServiceImpl extends ServiceImpl<ChatTagsMapper, ChatTags> implements IChatTagsService {

}
