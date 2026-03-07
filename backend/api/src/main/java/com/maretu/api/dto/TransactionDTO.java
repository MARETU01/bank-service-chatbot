package com.maretu.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易记录DTO
 */
@Data
@Accessors(chain = true)
public class TransactionDTO {

    /**
     * 交易ID
     */
    private Long id;

    /**
     * 交易流水号
     */
    private String transactionId;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 交易类型: DEPOSIT-存款, WITHDRAW-取款, TRANSFER_IN-转入, TRANSFER_OUT-转出, PAYMENT-支付
     */
    private String transactionType;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 交易后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 交易描述
     */
    private String description;

    /**
     * 对方账号
     */
    private String counterpartyAccount;

    /**
     * 对方名称
     */
    private String counterpartyName;

    /**
     * 状态: 0-失败, 1-成功, 2-处理中
     */
    private Integer status;

    /**
     * 交易时间
     */
    private LocalDateTime transactionTime;
}
