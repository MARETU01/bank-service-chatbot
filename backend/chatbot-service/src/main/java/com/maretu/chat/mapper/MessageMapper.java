package com.maretu.chat.mapper;

import com.maretu.chat.dto.AiMetadataStatsDTO;
import com.maretu.chat.pojo.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 聊天消息表 Mapper 接口
 * </p>
 *
 * @author maretu
 * @since 2026-03-14
 */
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 聚合查询 AI 元数据统计信息（从 ai_metadata JSON 字段中提取）
     */
    @Select("SELECT " +
            "  JSON_UNQUOTE(JSON_EXTRACT(ai_metadata, '$.model')) AS modelName, " +
            "  COALESCE(SUM(JSON_EXTRACT(ai_metadata, '$.totalTokens')), 0) AS totalTokens, " +
            "  COALESCE(SUM(JSON_EXTRACT(ai_metadata, '$.promptTokens')), 0) AS totalPromptTokens, " +
            "  COALESCE(SUM(JSON_EXTRACT(ai_metadata, '$.completionTokens')), 0) AS totalCompletionTokens, " +
            "  COALESCE(SUM(CASE WHEN DATE(created_at) = CURDATE() THEN JSON_EXTRACT(ai_metadata, '$.totalTokens') ELSE 0 END), 0) AS todayTokens, " +
            "  COALESCE(AVG(JSON_EXTRACT(ai_metadata, '$.durationMs')), 0) AS avgResponseTimeMs, " +
            "  COALESCE(MAX(JSON_EXTRACT(ai_metadata, '$.durationMs')), 0) AS maxResponseTimeMs " +
            "FROM message " +
            "WHERE sender_type = 2 AND ai_metadata IS NOT NULL")
    AiMetadataStatsDTO selectAiMetadataStats();
}
