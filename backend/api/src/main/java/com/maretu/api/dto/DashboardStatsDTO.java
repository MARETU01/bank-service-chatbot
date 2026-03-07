package com.maretu.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 仪表盘统计数据DTO
 */
@Data
@Accessors(chain = true)
public class DashboardStatsDTO {

    /**
     * 总资产
     */
    private BigDecimal totalBalance;

    /**
     * 总收入
     */
    private BigDecimal income;

    /**
     * 总支出
     */
    private BigDecimal expense;

    /**
     * 账户数量
     */
    private Integer accountCount;

    /**
     * 交易笔数
     */
    private Integer transactionCount;
}
