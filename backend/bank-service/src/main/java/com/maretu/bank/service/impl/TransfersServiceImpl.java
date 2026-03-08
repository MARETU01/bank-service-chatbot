package com.maretu.bank.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maretu.bank.dto.TransferReq;
import com.maretu.bank.pojo.AccountDailyLimits;
import com.maretu.bank.pojo.Accounts;
import com.maretu.bank.pojo.Transactions;
import com.maretu.bank.pojo.Transfers;
import com.maretu.bank.mapper.TransfersMapper;
import com.maretu.bank.service.IAccountDailyLimitsService;
import com.maretu.bank.service.IAccountsService;
import com.maretu.bank.service.ITransactionsService;
import com.maretu.bank.service.ITransfersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * 转账记录表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-03-07
 */
@Service
@RequiredArgsConstructor
public class TransfersServiceImpl extends ServiceImpl<TransfersMapper, Transfers> implements ITransfersService {

    private final IAccountsService accountsService;
    private final IAccountDailyLimitsService dailyLimitsService;
    private final ITransactionsService transactionsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Transfers executeTransfer(TransferReq req) {
        // 1. 查询转出账户
        Accounts fromAccount = accountsService.lambdaQuery()
                .eq(Accounts::getId, req.getFromAccountId())
                .one();

        if (fromAccount == null) {
            throw new RuntimeException("转出账户不存在");
        }

        // 2. 检查账户状态
        if (fromAccount.getStatus() != 1) {
            throw new RuntimeException("账户状态异常，无法转账");
        }

        // 3. 检查余额
        if (fromAccount.getBalance().compareTo(req.getAmount()) < 0) {
            throw new RuntimeException("账户余额不足");
        }

        // 4. 检查日限额
        checkDailyLimit(fromAccount, req.getAmount());

        // 5. 查询目标账户
        Accounts toAccount = accountsService.lambdaQuery()
                .eq(Accounts::getAccountNumber, req.getToAccountNumber())
                .one();

        if (toAccount == null) {
            throw new RuntimeException("目标账户不存在");
        }

        // 6. 检查目标账户状态
        if (toAccount.getStatus() != 1) {
            throw new RuntimeException("目标账户状态异常");
        }

        // 7. 生成转账流水号
        String transferId = "TRF" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        // 8. 扣减转出账户余额
        BigDecimal fromNewBalance = fromAccount.getBalance().subtract(req.getAmount());
        fromAccount.setBalance(fromNewBalance);
        accountsService.updateById(fromAccount);

        // 9. 增加目标账户余额
        BigDecimal toNewBalance = toAccount.getBalance().add(req.getAmount());
        toAccount.setBalance(toNewBalance);
        accountsService.updateById(toAccount);

        // 10. 更新日限额使用
        updateDailyLimitUsage(fromAccount, req.getAmount());

        // 11. 创建转出交易记录
        String fromTransactionId = "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Transactions transaction = new Transactions()
                .setTransactionId(fromTransactionId)
                .setAccountId(req.getFromAccountId())
                .setTransactionType("TRANSFER_OUT")
                .setAmount(req.getAmount())
                .setBalanceAfter(fromNewBalance)
                .setDescription("转账给" + (req.getToAccountName() == null ? "" : req.getToAccountName()))
                .setCounterpartyAccount(req.getToAccountNumber().trim())
                .setCounterpartyName(req.getToAccountName())
                .setStatus(1)
                .setTransactionTime(LocalDateTime.now());
        transactionsService.save(transaction);

        // 12. 创建转入交易记录
        String toTransactionId = "TXN" + System.currentTimeMillis() + "IN" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Transactions toTransaction = new Transactions()
                .setTransactionId(toTransactionId)
                .setAccountId(toAccount.getId())
                .setTransactionType("TRANSFER_IN")
                .setAmount(req.getAmount())
                .setBalanceAfter(toNewBalance)
                .setDescription("收到" + fromAccount.getAccountName() + "转账")
                .setCounterpartyAccount(fromAccount.getAccountNumber())
                .setCounterpartyName(fromAccount.getAccountName())
                .setStatus(1)
                .setTransactionTime(LocalDateTime.now());
        transactionsService.save(toTransaction);

        // 13. 创建转账记录
        Transfers transfer = new Transfers()
                .setTransferId(transferId)
                .setFromAccountId(req.getFromAccountId())
                .setToAccountId(toAccount.getId())
                .setToAccountNumber(req.getToAccountNumber())
                .setToAccountName(req.getToAccountName())
                .setAmount(req.getAmount())
                .setRemark(req.getRemark())
                .setStatus(1);

        save(transfer);

        return transfer;
    }

    @Override
    public List<Transfers> getTransfersByAccountId(Long accountId, Integer page, Integer size) {
        return lambdaQuery()
                .eq(Transfers::getFromAccountId, accountId)
                .orderByDesc(Transfers::getCreatedAt)
                .page(new Page<>(page, size))
                .getRecords();
    }

    @Override
    public List<Transfers> getTransfersByAccountIds(List<Long> accountIds, Integer page, Integer size) {
        if (accountIds == null || accountIds.isEmpty()) {
            return List.of();
        }
        return lambdaQuery()
                .in(Transfers::getFromAccountId, accountIds)
                .orderByDesc(Transfers::getCreatedAt)
                .page(new Page<>(page, size))
                .getRecords();
    }

    /**
     * 检查日限额
     */
    private void checkDailyLimit(Accounts account, BigDecimal amount) {
        LocalDate today = LocalDate.now();

        // 查询当日已用额度
        AccountDailyLimits dailyLimit = dailyLimitsService.lambdaQuery()
                .eq(AccountDailyLimits::getAccountId, account.getId())
                .eq(AccountDailyLimits::getLimitDate, today)
                .one();

        BigDecimal usedAmount = dailyLimit != null ? dailyLimit.getUsedAmount() : BigDecimal.ZERO;

        // 检查是否超过日限额
        if (usedAmount.add(amount).compareTo(account.getDailyLimit()) > 0) {
            throw new RuntimeException("已超过单日交易限额");
        }
    }

    /**
     * 更新日限额使用
     */
    private void updateDailyLimitUsage(Accounts account, BigDecimal amount) {
        LocalDate today = LocalDate.now();

        AccountDailyLimits dailyLimit = dailyLimitsService.lambdaQuery()
                .eq(AccountDailyLimits::getAccountId, account.getId())
                .eq(AccountDailyLimits::getLimitDate, today)
                .one();

        if (dailyLimit == null) {
            dailyLimit = new AccountDailyLimits()
                    .setAccountId(account.getId())
                    .setLimitDate(today)
                    .setDailyLimit(account.getDailyLimit())
                    .setUsedAmount(amount);
            dailyLimitsService.save(dailyLimit);
        } else {
            dailyLimit.setUsedAmount(dailyLimit.getUsedAmount().add(amount));
            dailyLimitsService.updateById(dailyLimit);
        }
    }
}
