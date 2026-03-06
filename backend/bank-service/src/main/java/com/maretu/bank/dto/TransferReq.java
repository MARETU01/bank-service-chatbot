package com.maretu.bank.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 转账请求DTO
 */
@Data
@Accessors(chain = true)
public class TransferReq {

    /**
     * 转出账户ID
     */
    private Long fromAccountId;

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
     * 转账备注
     */
    private String remark;

    /**
     * 支付密码
     */
    private String payPassword;
}
