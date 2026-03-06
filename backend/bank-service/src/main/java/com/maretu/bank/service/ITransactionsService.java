package com.maretu.bank.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maretu.bank.dto.TransactionQueryReq;
import com.maretu.bank.pojo.Transactions;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 交易记录表 服务类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
public interface ITransactionsService extends IService<Transactions> {

    /**
     * 分页查询交易记录
     */
    Page<Transactions> queryTransactions(Long userId, TransactionQueryReq req);

    /**
     * 根据账户ID查询交易记录
     */
    Page<Transactions> getTransactionsByAccountId(Long accountId, Integer page, Integer size);
}
