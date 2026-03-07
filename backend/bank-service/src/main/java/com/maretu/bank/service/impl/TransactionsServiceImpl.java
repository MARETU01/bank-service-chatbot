package com.maretu.bank.service.impl;

import com.maretu.bank.dto.TransactionQueryReq;
import com.maretu.bank.pojo.Transactions;
import com.maretu.bank.mapper.TransactionsMapper;
import com.maretu.bank.service.ITransactionsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * <p>
 * 交易记录表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-03-07
 */
@Service
public class TransactionsServiceImpl extends ServiceImpl<TransactionsMapper, Transactions> implements ITransactionsService {

    @Override
    public List<Transactions> queryTransactions(TransactionQueryReq req) {
        return lambdaQuery()
                .eq(req.getAccountId() != null, Transactions::getAccountId, req.getAccountId())
                .eq(StringUtils.hasText(req.getTransactionType()), Transactions::getTransactionType, req.getTransactionType())
                .eq(req.getStatus() != null, Transactions::getStatus, req.getStatus())
                .ge(StringUtils.hasText(req.getStartDate()), Transactions::getTransactionTime, 
                        StringUtils.hasText(req.getStartDate()) ? LocalDateTime.parse(req.getStartDate() + "T00:00:00") : null)
                .le(StringUtils.hasText(req.getEndDate()), Transactions::getTransactionTime, 
                        StringUtils.hasText(req.getEndDate()) ? LocalDateTime.parse(req.getEndDate() + "T23:59:59") : null)
                .orderByDesc(Transactions::getTransactionTime)
                .page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(req.getPage(), req.getSize()))
                .getRecords();
    }
}
