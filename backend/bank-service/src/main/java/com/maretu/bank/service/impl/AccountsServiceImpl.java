package com.maretu.bank.service.impl;

import com.maretu.bank.dto.DashboardStats;
import com.maretu.bank.pojo.Accounts;
import com.maretu.bank.mapper.AccountsMapper;
import com.maretu.bank.pojo.Transactions;
import com.maretu.bank.service.IAccountsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maretu.bank.service.ITransactionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 账户表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Service
public class AccountsServiceImpl extends ServiceImpl<AccountsMapper, Accounts> implements IAccountsService {

    @Lazy
    @Autowired
    private ITransactionsService transactionsService;

    @Override
    public List<Accounts> getAccountsByUserId(Long userId) {
        return lambdaQuery()
                .eq(Accounts::getUserId, userId)
                .orderByAsc(Accounts::getId)
                .list();
    }

    @Override
    public Accounts getAccountDetail(Long userId, Long accountId) {
        return lambdaQuery()
                .eq(Accounts::getId, accountId)
                .eq(Accounts::getUserId, userId)
                .one();
    }

    @Override
    public DashboardStats getDashboardStats(Long userId) {
        // 获取用户所有账户
        List<Accounts> accounts = getAccountsByUserId(userId);
        
        // 计算总资产
        BigDecimal totalBalance = accounts.stream()
                .map(Accounts::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 统计账户数量
        int accountCount = accounts.size();
        
        // 获取所有账户ID
        List<Long> accountIds = accounts.stream()
                .map(Accounts::getId)
                .toList();
        
        // 统计交易数据
        int transactionCount = 0;
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        
        if (!accountIds.isEmpty()) {
            List<Transactions> transactions = transactionsService.lambdaQuery()
                    .in(Transactions::getAccountId, accountIds)
                    .eq(Transactions::getStatus, 1)
                    .list();
            
            transactionCount = transactions.size();
            
            for (Transactions t : transactions) {
                String type = t.getTransactionType();
                if ("DEPOSIT".equals(type) || "TRANSFER_IN".equals(type)) {
                    income = income.add(t.getAmount());
                } else if ("WITHDRAW".equals(type) || "TRANSFER_OUT".equals(type) || "PAYMENT".equals(type)) {
                    expense = expense.add(t.getAmount());
                }
            }
        }

        return new DashboardStats()
                .setAccountCount(accountCount)
                .setIncome(income)
                .setExpense(expense)
                .setTotalBalance(totalBalance)
                .setTransactionCount(transactionCount);
    }
}
