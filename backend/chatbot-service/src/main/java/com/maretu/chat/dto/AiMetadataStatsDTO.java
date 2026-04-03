package com.maretu.chat.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * AI 元数据聚合统计结果（Mapper 查询映射用）
 *
 * @author maretu
 */
@Data
@Accessors(chain = true)
public class AiMetadataStatsDTO {

    /** 模型名称 */
    private String modelName;

    /** 总 Token 消耗 */
    private Long totalTokens;

    /** 总输入 Token */
    private Long totalPromptTokens;

    /** 总输出 Token */
    private Long totalCompletionTokens;

    /** 今日 Token 消耗 */
    private Long todayTokens;

    /** 平均响应时间（毫秒） */
    private Double avgResponseTimeMs;

    /** 最大响应时间（毫秒） */
    private Long maxResponseTimeMs;
}
