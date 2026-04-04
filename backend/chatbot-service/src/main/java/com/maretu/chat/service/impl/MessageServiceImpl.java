package com.maretu.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maretu.api.client.UserClient;
import com.maretu.chat.dto.AiMetadataStatsDTO;
import com.maretu.chat.dto.ChatStatsDTO;
import com.maretu.chat.mapper.MessageMapper;
import com.maretu.chat.pojo.Message;
import com.maretu.chat.pojo.Session;
import com.maretu.chat.service.IMessageService;
import com.maretu.chat.service.ISessionService;
import com.maretu.common.dto.Context;
import com.maretu.common.utils.ChatGuardUtils;
import com.maretu.common.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;


/**
 * <p>
 * 聊天消息表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-03-14
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

    private final ObjectMapper jacksonObjectMapper;
    private final ISessionService sessionService;
    private final ChatClient chatClient;
    private final Executor virtualThreadPoolExecutor;
    private final UserClient userClient;

    @Value("${spring.ai.openai.chat.options.model:unknown}")
    private String modelName;

    @Override
    public List<Message> getMessages(Integer userId, String sessionId) {
        if (!sessionService.isSessionOwner(userId, sessionId)) {
            throw new RuntimeException("无权访问该会话的消息");
        }
        return lambdaQuery()
                .eq(Message::getSessionId, sessionId)
                .orderByAsc(Message::getCreatedAt)
                .list();
    }

    @Override
    public Flux<String> chat(String userJson, Message message) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        if (!sessionService.isSessionOwner(context.getUserId(), message.getSessionId())) {
            throw new RuntimeException("无权访问该会话的消息");
        }

        // ===== 输入安全防护 =====
        ChatGuardUtils.GuardResult guardResult = ChatGuardUtils.processInput(message.getContent());
        if (!guardResult.isPassed()) {
            // 输入未通过安全检查，直接返回拒绝原因，同时保存对话记录
            String rejectReason = guardResult.getRejectReason();
            virtualThreadPoolExecutor.execute(() -> {
                List<Message> saveMessages = new ArrayList<>();
                saveMessages.add(message.setSenderType(1).setMessageType("TEXT"));
                saveMessages.add(new Message()
                        .setMessageType("TEXT")
                        .setSenderType(2)
                        .setSessionId(message.getSessionId())
                        .setContent(rejectReason));
                saveBatch(saveMessages, 2);
            });
            return Flux.just(rejectReason);
        }
        // 使用安全处理后的内容（已脱敏、已清理）
        String safeContent = guardResult.getSafeContent();

        // ===== 调用 LLM =====
        StringBuilder fullResponse = new StringBuilder();
        long startTime = System.currentTimeMillis();
        AtomicLong promptTokens = new AtomicLong(0);
        AtomicLong completionTokens = new AtomicLong(0);

        return chatClient.prompt()
                .user(safeContent)
                .messages(getRecentMessages(message.getSessionId(), 20))
                .toolContext(Map.of("context", userJson))
                .stream()
                .chatResponse()
                .doOnNext(chatResponse -> {
                    // 累积 token 统计信息
                    if (chatResponse != null && chatResponse.getMetadata().getUsage() != null) {
                        var usage = chatResponse.getMetadata().getUsage();
                        if (usage.getPromptTokens() > 0) {
                            promptTokens.set(usage.getPromptTokens());
                        }
                        if (usage.getCompletionTokens() > 0) {
                            completionTokens.addAndGet(usage.getCompletionTokens());
                        }
                    }
                })
                // 提取文本内容用于前端流式输出
                .map(chatResponse -> {
                    chatResponse.getResult();
                    chatResponse.getResult();
                    if (chatResponse.getResult().getOutput().getText() != null) {
                        String text = chatResponse.getResult().getOutput().getText();
                        fullResponse.append(text);
                        return text;
                    }
                    return "";
                })
                .filter(text -> !text.isEmpty())
                // ===== 输出安全审核（逐片段累积后在完成时审核） =====
                .doOnComplete(() -> virtualThreadPoolExecutor.execute(() -> {
                    long durationMs = System.currentTimeMillis() - startTime;
                    // 对完整回答进行输出审核
                    String reviewedResponse = ChatGuardUtils.reviewOutput(fullResponse.toString());
                    if (!reviewedResponse.contentEquals(fullResponse)) {
                        System.out.printf("用户 [%s] 的 AI 回答未通过输出审核，已替换为安全回答%n", context.getUserId());
                    }

                    // 构建 AI 元数据
                    String aiMetadataJson = null;
                    try {
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("model", modelName);
                        metadata.put("promptTokens", promptTokens.get());
                        metadata.put("completionTokens", completionTokens.get());
                        metadata.put("totalTokens", promptTokens.get() + completionTokens.get());
                        metadata.put("durationMs", durationMs);
                        aiMetadataJson = jacksonObjectMapper.writeValueAsString(metadata);
                    } catch (JsonProcessingException e) {
                        log.error("序列化 AI 元数据失败", e);
                    }

                    List<Message> saveMessages = new ArrayList<>();
                    saveMessages.add(message.setSenderType(1).setMessageType("TEXT"));
                    saveMessages.add(new Message()
                            .setMessageType("TEXT")
                            .setSenderType(2)
                            .setSessionId(message.getSessionId())
                            .setContent(reviewedResponse)
                            .setAiMetadata(aiMetadataJson));
                    saveBatch(saveMessages, 2);
                }));
    }

    /**
     * 检查用户是否具有 admin 角色
     */
    private void checkAdminRole(String userJson) {
        Result<List<String>> result = userClient.getUserRoles(userJson);
        if (result == null || result.getData() == null) {
            throw new RuntimeException("获取用户角色失败");
        }
        List<String> roles = result.getData();
        if (roles.isEmpty() || !roles.contains("ADMIN")) {
            throw new RuntimeException("权限不足：需要 admin 角色才能执行此操作");
        }
    }

    @Override
    public ChatStatsDTO getChatStats(String userJson, String startDate, String endDate) {
        // 检查 admin 角色
        checkAdminRole(userJson);

        ChatStatsDTO stats = new ChatStatsDTO();
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        // 解析时间范围参数
        LocalDateTime rangeStart = null;
        LocalDateTime rangeEnd = null;
        if (startDate != null && !startDate.isEmpty()) {
            rangeStart = LocalDate.parse(startDate).atStartOfDay();
        }
        if (endDate != null && !endDate.isEmpty()) {
            rangeEnd = LocalDate.parse(endDate).atTime(LocalTime.MAX);
        }

        // 用于 lambda 闭包的 final 变量
        final LocalDateTime finalRangeStart = rangeStart;
        final LocalDateTime finalRangeEnd = rangeEnd;

        // 传给 Mapper 的日期字符串（用于 AI 元数据聚合 SQL）
        final String sqlStartDate = rangeStart != null ? rangeStart.toString() : null;
        final String sqlEndDate = rangeEnd != null ? rangeEnd.toString() : null;

        // ===== 所有查询互不依赖，使用虚拟线程并发执行 =====

        // 总会话数
        CompletableFuture<Long> totalSessionsFuture = CompletableFuture.supplyAsync(() -> sessionService
                .lambdaQuery().eq(Session::getDeleted, 0)
                .ge(finalRangeStart != null, Session::getCreatedAt, finalRangeStart)
                .le(finalRangeEnd != null, Session::getCreatedAt, finalRangeEnd)
                .count(), virtualThreadPoolExecutor);

        // 总消息数
        CompletableFuture<Long> totalMessagesFuture = CompletableFuture.supplyAsync(() -> lambdaQuery()
                .ge(finalRangeStart != null, Message::getCreatedAt, finalRangeStart)
                .le(finalRangeEnd != null, Message::getCreatedAt, finalRangeEnd)
                .count(), virtualThreadPoolExecutor);

        // 用户消息数
        CompletableFuture<Long> userMessagesFuture = CompletableFuture.supplyAsync(() -> lambdaQuery()
                .eq(Message::getSenderType, 1)
                .ge(finalRangeStart != null, Message::getCreatedAt, finalRangeStart)
                .le(finalRangeEnd != null, Message::getCreatedAt, finalRangeEnd)
                .count(), virtualThreadPoolExecutor);

        // AI 回复数
        CompletableFuture<Long> aiMessagesFuture = CompletableFuture.supplyAsync(() -> lambdaQuery()
                .eq(Message::getSenderType, 2)
                .ge(finalRangeStart != null, Message::getCreatedAt, finalRangeStart)
                .le(finalRangeEnd != null, Message::getCreatedAt, finalRangeEnd)
                .count(), virtualThreadPoolExecutor);

        // 活跃用户数（有过对话的用户，按 user_id 分组）
        CompletableFuture<Long> activeUsersFuture = CompletableFuture.supplyAsync(() -> (long) sessionService
                .lambdaQuery().eq(Session::getDeleted, 0)
                .select(Session::getUserId)
                .ge(finalRangeStart != null, Session::getCreatedAt, finalRangeStart)
                .le(finalRangeEnd != null, Session::getCreatedAt, finalRangeEnd)
                .groupBy(Session::getUserId)
                .list()
                .size(), virtualThreadPoolExecutor);

        // 今日新增会话数
        CompletableFuture<Long> todaySessionsFuture = CompletableFuture.supplyAsync(() -> sessionService
                .lambdaQuery().eq(Session::getDeleted, 0)
                .ge(Session::getCreatedAt, todayStart)
                .count(), virtualThreadPoolExecutor);

        // 今日消息数
        CompletableFuture<Long> todayMessagesFuture = CompletableFuture.supplyAsync(() -> lambdaQuery()
                .ge(Message::getCreatedAt, todayStart)
                .count(), virtualThreadPoolExecutor);

        // AI 元数据聚合统计（支持时间范围）
        CompletableFuture<AiMetadataStatsDTO> aiStatsFuture = CompletableFuture.supplyAsync(() ->
                getBaseMapper().selectAiMetadataStats(sqlStartDate, sqlEndDate), virtualThreadPoolExecutor);

        // 被拦截的消息数
        CompletableFuture<Long> blockedMessagesFuture = CompletableFuture.supplyAsync(() -> lambdaQuery()
                .eq(Message::getSenderType, 2)
                .isNull(Message::getAiMetadata)
                .ge(finalRangeStart != null, Message::getCreatedAt, finalRangeStart)
                .le(finalRangeEnd != null, Message::getCreatedAt, finalRangeEnd)
                .count(), virtualThreadPoolExecutor);

        // 等待所有查询完成
        CompletableFuture.allOf(
                totalSessionsFuture, totalMessagesFuture, userMessagesFuture,
                aiMessagesFuture, activeUsersFuture, todaySessionsFuture,
                todayMessagesFuture, aiStatsFuture, blockedMessagesFuture
        ).join();

        // ===== 汇总结果 =====
        try {
            Long totalSessions = totalSessionsFuture.get();
            Long totalMessages = totalMessagesFuture.get();
            Long userMessages = userMessagesFuture.get();
            Long aiMessages = aiMessagesFuture.get();
            Long blockedMessages = blockedMessagesFuture.get();

            // 基础对话统计
            stats.setTotalSessions(totalSessions)
                    .setTotalMessages(totalMessages)
                    .setUserMessages(userMessages)
                    .setAiMessages(aiMessages)
                    .setActiveUsers(activeUsersFuture.get())
                    .setTodaySessions(todaySessionsFuture.get())
                    .setTodayMessages(todayMessagesFuture.get())
                    .setAvgMessagesPerSession(
                            totalSessions > 0
                                    ? Math.round((double) totalMessages / totalSessions * 100.0) / 100.0
                                    : 0.0
                    );

            // AI 性能统计
            AiMetadataStatsDTO aiStats = aiStatsFuture.get();
            if (aiStats != null) {
                stats.setModelName(aiStats.getModelName())
                        .setTotalTokens(aiStats.getTotalTokens())
                        .setTotalPromptTokens(aiStats.getTotalPromptTokens())
                        .setTotalCompletionTokens(aiStats.getTotalCompletionTokens())
                        .setTodayTokens(aiStats.getTodayTokens())
                        .setAvgResponseTimeMs(
                                Math.round(aiStats.getAvgResponseTimeMs() * 100.0) / 100.0
                        )
                        .setMaxResponseTimeMs(aiStats.getMaxResponseTimeMs())
                        .setAvgTokensPerChat(
                                aiMessages > 0 && aiStats.getTotalTokens() != null
                                        ? Math.round((double) aiStats.getTotalTokens() / aiMessages * 100.0) / 100.0
                                        : 0.0
                        );
            }

            // 安全防护统计
            stats.setBlockedMessages(blockedMessages)
                    .setBlockRate(
                            userMessages > 0
                                    ? Math.round((double) blockedMessages / userMessages * 100.0) / 100.0
                                    : 0.0
                    );
        } catch (Exception e) {
            log.error("并发查询统计数据异常", e);
            throw new RuntimeException("获取统计数据失败：" + e.getMessage());
        }

        return stats;
    }

    @Override
    public List<org.springframework.ai.chat.messages.Message> getRecentMessages(String sessionId, Integer limit) {
        List<Message> dbMessages = lambdaQuery()
                .eq(Message::getSessionId, sessionId)
                .orderByDesc(Message::getCreatedAt)
                .last("LIMIT " + limit)
                .list()
                .reversed();
        List<org.springframework.ai.chat.messages.Message> contextMessages = new ArrayList<>();
        for (Message message : dbMessages) {
            if (message.getSenderType() == 1) {
                contextMessages.add(new UserMessage(message.getContent()));
            } else if (message.getSenderType() == 2) {
                contextMessages.add(new AssistantMessage(message.getContent()));
            }
        }
        return contextMessages;
    }
}
