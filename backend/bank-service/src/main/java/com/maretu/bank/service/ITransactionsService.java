package com.maretu.bank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maretu.bank.dto.TransactionQueryReq;
import com.maretu.bank.pojo.Transactions;

import java.util.List;

/**
 * <p>
 * 交易记录表 服务类
 * </p>
 *
 * @author maretu
 * @since 2026-03-07
 */
public interface ITransactionsService extends IService<Transactions> {

    /**
     * 查询交易记录
     */
    List<Transactions> queryTransactions(TransactionQueryReq req);
}
