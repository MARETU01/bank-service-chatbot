package com.maretu.chat.service.impl;

import com.maretu.chat.pojo.Faqs;
import com.maretu.chat.mapper.FaqsMapper;
import com.maretu.chat.service.IFaqsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 常见问题表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Service
public class FaqsServiceImpl extends ServiceImpl<FaqsMapper, Faqs> implements IFaqsService {

}
