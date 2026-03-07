package com.maretu.bank.pojo;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 交易记录表
 * </p>
 *
 * @author maretu
 * @since 2026-03-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("transactions")
public class Transactions implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 交易ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;


}
