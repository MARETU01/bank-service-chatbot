package com.maretu.bank.service.impl;

import com.maretu.bank.dto.DashboardStats;
import com.maretu.bank.pojo.Accounts;
import com.maretu.bank.mapper.AccountsMapper;
import com.maretu.bank.pojo.Transactions;
import com.maretu.bank.service.IAccountsService;
import com.maretu.bank.service.ITransactionsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 账户表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-03-07
 */
@Service
@RequiredArgsConstructor
public class AccountsServiceImpl extends ServiceImpl<AccountsMapper, Accounts> implements IAccountsService {

    private final ITransactionsService transactionsService;

    @Override
    public List<Accounts> getAccountsByUserId(Long userId) {
        return lambdaQuery()
                .eq(Accounts::getUserId, userId)
                .orderByDesc(Accounts::getCreatedAt)
                .list();
    }

    @Override
    public Accounts getAccountByIdAndUserId(Long accountId, Long userId) {
        return lambdaQuery()
                .eq(Accounts::getId, accountId)
                .eq(Accounts::getUserId, userId)
                .one();
    }

    @Override
    public DashboardStats getDashboardStats(Long userId) {
        // 获取用户所有账户
        List<Accounts> accounts = getAccountsByUserId(userId);
        
        if (accounts.isEmpty()) {
            return new DashboardStats()
                    .setTotalBalance(BigDecimal.ZERO)
                    .setIncome(BigDecimal.ZERO)
                    .setExpense(BigDecimal.ZERO)
                    .setAccountCount(0)
                    .setTransactionCount(0);
        }
        
        // 计算总资产
        BigDecimal totalBalance = accounts.stream()
                .filter(a -> a.getStatus() == 1)
                .map(Accounts::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 获取账户ID列表
        List<Long> accountIds = accounts.stream()
                .map(Accounts::getId)
                .toList();
        
        // 查询所有交易记录
        List<Transactions> transactions = transactionsService.lambdaQuery()
                .in(Transactions::getAccountId, accountIds)
                .eq(Transactions::getStatus, 1)
                .list();
        
        // 计算收入（存款、转入）
        BigDecimal income = transactions.stream()
                .filter(t -> "DEPOSIT".equals(t.getTransactionType()) || "TRANSFER_IN".equals(t.getTransactionType()))
                .map(Transactions::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 计算支出（取款、转出、支付）
        BigDecimal expense = transactions.stream()
                .filter(t -> "WITHDRAW".equals(t.getTransactionType()) || "TRANSFER_OUT".equals(t.getTransactionType()) || "PAYMENT".equals(t.getTransactionType()))
                .map(Transactions::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new DashboardStats()
                .setTotalBalance(totalBalance)
                .setIncome(income)
                .setExpense(expense)
                .setAccountCount(accounts.size())
                .setTransactionCount(transactions.size());
    }
}
