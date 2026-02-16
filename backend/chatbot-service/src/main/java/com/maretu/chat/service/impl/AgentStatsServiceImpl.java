package com.maretu.chat.service.impl;

import com.maretu.chat.pojo.AgentStats;
import com.maretu.chat.mapper.AgentStatsMapper;
import com.maretu.chat.service.IAgentStatsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 客服工作统计表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Service
public class AgentStatsServiceImpl extends ServiceImpl<AgentStatsMapper, AgentStats> implements IAgentStatsService {

}
