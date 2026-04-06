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
import java.util.Random;

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
    public Accounts createAccount(Long userId, Accounts req) {
        // 生成唯一账号 (16位数字)
        String accountNumber = generateAccountNumber();

        // 检查账号是否已存在
        while (lambdaQuery().eq(Accounts::getAccountNumber, accountNumber).count() > 0) {
            accountNumber = generateAccountNumber();
        }

        Accounts account = new Accounts()
                .setUserId(userId)
                .setAccountNumber(accountNumber)
                .setAccountName(req.getAccountName() != null ? req.getAccountName() : "储蓄账户")
                .setBalance(BigDecimal.ZERO)
                .setCurrency(req.getCurrency() != null ? req.getCurrency() : "CNY")
                .setStatus(1)
                .setDailyLimit(req.getDailyLimit() != null ? req.getDailyLimit() : new BigDecimal("50000.00"));
        save(account);
        return account;
    }

    @Override
    public List<Accounts> getAccountsByUserId(Long userId) {
        return lambdaQuery()
                .eq(Accounts::getUserId, userId)
                .orderByDesc(Accounts::getCreatedAt)
                .list();
    }

    @Override
    public List<Long> getAccountIdsByUserId(Long userId) {
        return lambdaQuery()
                .eq(Accounts::getUserId, userId)
                .list()
                .stream()
                .map(Accounts::getId)
                .toList();
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

    @Override
    public Accounts updateAccount(Long accountId, Long userId, Accounts req) {
        Accounts account = getAccountByIdAndUserId(accountId, userId);
        if (account == null) {
            throw new RuntimeException("Account does not exist or no permission to access");
        }

        lambdaUpdate().eq(Accounts::getId, account.getId())
                    .set(req.getAccountName() != null, Accounts::getAccountName, account.getAccountName())
                    .set(req.getDailyLimit() != null, Accounts::getDailyLimit, account.getDailyLimit())
                    .set(req.getBalance() != null, Accounts::getBalance, req.getBalance())
                    .update();

        return getAccountByIdAndUserId(accountId, userId);
    }

    @Override
    public Accounts updateStatus(Long accountId, Long userId, Integer status) {
        Accounts account = getAccountByIdAndUserId(accountId, userId);
        if (account == null) {
            throw new RuntimeException("Account does not exist or no permission to access");
        }
        
        // Validate status value
        if (status < 0 || status > 2) {
            throw new RuntimeException("Invalid status value");
        }
        
        // 不允许将状态改为关闭(2)，关闭需要单独的流程
        if (status == 2) {
            throw new RuntimeException("Closing accounts directly is not supported, please contact customer service");
        }
        
        account.setStatus(status);
        updateById(account);
        return account;
    }

    /**
     * 生成16位随机账号
     */
    private String generateAccountNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("62"); // 以62开头，模拟银行卡号
        for (int i = 0; i < 14; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
