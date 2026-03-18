package com.maretu.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户 DTO
 */
@Data
@Accessors(chain = true)
public class AccountDTO {

    /**
     * 账户 ID
     */
    private Long id;

    /**
     * 账号
     */
    private String accountNumber;

    /**
     * 用户 ID(关联用户服务)
     */
    private Long userId;

    /**
     * 账户名称
     */
    private String accountName;

    /**
     * 账户余额
     */
    private BigDecimal balance;

    /**
     * 货币类型
     */
    private String currency;

    /**
     * 状态：0-冻结，1-正常，2-关闭
     */
    private Integer status;

    /**
     * 单日交易限额
     */
    private BigDecimal dailyLimit;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
