package com.maretu.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 交易记录查询请求 DTO
 */
@Data
@Accessors(chain = true)
public class TransactionQueryReqDTO {

    /**
     * 账户 ID
     */
    private Long accountId;

    /**
     * 交易类型：DEPOSIT-存款，WITHDRAW-取款，TRANSFER_IN-转入，TRANSFER_OUT-转出，PAYMENT-支付
     */
    private String transactionType;

    /**
     * 状态：0-失败，1-成功，2-处理中
     */
    private Integer status;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer size = 10;

    /**
     * 是否查询所有账户的交易记录
     */
    private Boolean allAccounts = false;

    /**
     * 用户 ID（当 allAccounts=true 时使用）
     */
    private Long userId;
}
