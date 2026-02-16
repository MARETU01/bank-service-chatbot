package com.maretu.chat.service.impl;

import com.maretu.chat.pojo.Attachments;
import com.maretu.chat.mapper.AttachmentsMapper;
import com.maretu.chat.service.IAttachmentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 附件表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Service
public class AttachmentsServiceImpl extends ServiceImpl<AttachmentsMapper, Attachments> implements IAttachmentsService {

}
