package com.maretu.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 转账记录 DTO
 */
@Data
@Accessors(chain = true)
public class TransferDTO {

    /**
     * 转账 ID
     */
    private Long id;

    /**
     * 转出账户 ID
     */
    private Long fromAccountId;

    /**
     * 转出账号
     */
    private String fromAccountNumber;

    /**
     * 转入账户 ID
     */
    private Long toAccountId;

    /**
     * 转入账号
     */
    private String toAccountNumber;

    /**
     * 转入账户名称
     */
    private String toAccountName;

    /**
     * 转入银行名称
     */
    private String toBankName;

    /**
     * 转账金额
     */
    private BigDecimal amount;

    /**
     * 手续费
     */
    private BigDecimal fee;

    /**
     * 转账状态：0-失败，1-成功，2-处理中
     */
    private Integer status;

    /**
     * 转账备注
     */
    private String remark;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
