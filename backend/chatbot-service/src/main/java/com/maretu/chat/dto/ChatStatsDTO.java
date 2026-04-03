package com.maretu.chat.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 对话统计概览 DTO
 *
 * @author maretu
 */
@Data
@Accessors(chain = true)
public class ChatStatsDTO {

    // ===== 基础对话统计 =====

    /** 总会话数 */
    private Long totalSessions;

    /** 总消息数 */
    private Long totalMessages;

    /** 用户消息数 */
    private Long userMessages;

    /** AI 回复数 */
    private Long aiMessages;

    /** 活跃用户数（有过对话的用户） */
    private Long activeUsers;

    /** 今日新增会话数 */
    private Long todaySessions;

    /** 今日消息数 */
    private Long todayMessages;

    /** 平均每会话消息数 */
    private Double avgMessagesPerSession;

    // ===== AI 性能统计（来自 ai_metadata）=====

    /** 当前使用的模型 */
    private String modelName;

    /** 总 Token 消耗 */
    private Long totalTokens;

    /** 总输入 Token */
    private Long totalPromptTokens;

    /** 总输出 Token */
    private Long totalCompletionTokens;

    /** 今日 Token 消耗 */
    private Long todayTokens;

    /** 平均每次 Token 消耗 */
    private Double avgTokensPerChat;

    /** 平均响应时间（毫秒） */
    private Double avgResponseTimeMs;

    /** 最大响应时间（毫秒） */
    private Long maxResponseTimeMs;

    // ===== 安全防护统计 =====

    /** 被安全拦截的消息数 */
    private Long blockedMessages;

    /** 拦截率（百分比） */
    private Double blockRate;
}
