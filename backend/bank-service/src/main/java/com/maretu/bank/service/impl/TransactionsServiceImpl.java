package com.maretu.bank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.maretu.bank.dto.TransactionQueryReq;
import com.maretu.bank.pojo.Accounts;
import com.maretu.bank.pojo.Transactions;
import com.maretu.bank.mapper.TransactionsMapper;
import com.maretu.bank.service.IAccountsService;
import com.maretu.bank.service.ITransactionsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 * 交易记录表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Service
@RequiredArgsConstructor
public class TransactionsServiceImpl extends ServiceImpl<TransactionsMapper, Transactions> implements ITransactionsService {

    private final IAccountsService accountsService;

    @Override
    public Page<Transactions> queryTransactions(Long userId, TransactionQueryReq req) {
        // 获取用户所有账户ID
        List<Long> accountIds = accountsService.getAccountsByUserId(userId).stream()
                .map(Accounts::getId)
                .toList();
        
        if (accountIds.isEmpty()) {
            return new Page<>(req.getPage(), req.getSize());
        }
        
        var chain = lambdaQuery()
                .in(Transactions::getAccountId, accountIds);
        
        // 按账户筛选
        if (req.getAccountId() != null) {
            chain.eq(Transactions::getAccountId, req.getAccountId());
        }
        
        // 按交易类型筛选
        if (req.getTransactionType() != null && !req.getTransactionType().isEmpty()) {
            chain.eq(Transactions::getTransactionType, req.getTransactionType());
        }
        
        // 按状态筛选
        if (req.getStatus() != null) {
            chain.eq(Transactions::getStatus, req.getStatus());
        }
        
        // 按日期范围筛选
        if (req.getStartDate() != null && !req.getStartDate().isEmpty()) {
            LocalDateTime start = LocalDateTime.parse(req.getStartDate() + " 00:00:00", 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            chain.ge(Transactions::getTransactionTime, start);
        }
        if (req.getEndDate() != null && !req.getEndDate().isEmpty()) {
            LocalDateTime end = LocalDateTime.parse(req.getEndDate() + " 23:59:59", 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            chain.le(Transactions::getTransactionTime, end);
        }
        
        return chain.orderByDesc(Transactions::getTransactionTime)
                .page(new Page<>(req.getPage(), req.getSize()));
    }

    @Override
    public Page<Transactions> getTransactionsByAccountId(Long accountId, Integer page, Integer size) {
        return lambdaQuery()
                .eq(Transactions::getAccountId, accountId)
                .orderByDesc(Transactions::getTransactionTime)
                .page(new Page<>(page, size));
    }
}
