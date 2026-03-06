package com.maretu.bank.service.impl;

import com.maretu.bank.dto.TransferReq;
import com.maretu.bank.pojo.AccountLimits;
import com.maretu.bank.pojo.Accounts;
import com.maretu.bank.pojo.Transactions;
import com.maretu.bank.pojo.Transfers;
import com.maretu.bank.mapper.TransfersMapper;
import com.maretu.bank.service.IAccountLimitsService;
import com.maretu.bank.service.IAccountsService;
import com.maretu.bank.service.ITransactionsService;
import com.maretu.bank.service.ITransfersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * <p>
 * 转账记录表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Service
@RequiredArgsConstructor
public class TransfersServiceImpl extends ServiceImpl<TransfersMapper, Transfers> implements ITransfersService {

    private final IAccountsService accountsService;
    private final ITransactionsService transactionsService;
    private final IAccountLimitsService accountLimitsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Transfers transfer(Long userId, TransferReq req) throws Exception {
        // 验证转出账户
        Accounts fromAccount = accountsService.lambdaQuery()
                .eq(Accounts::getId, req.getFromAccountId())
                .eq(Accounts::getUserId, userId)
                .one();
        
        if (fromAccount == null) {
            throw new Exception("转出账户不存在或无权限");
        }
        
        if (fromAccount.getStatus() != 1) {
            throw new Exception("账户状态异常，无法转账");
        }
        
        // 验证余额
        BigDecimal transferAmount = req.getAmount();
        if (fromAccount.getAvailableBalance().compareTo(transferAmount) < 0) {
            throw new Exception("账户余额不足");
        }
        
        // 验证限额
        AccountLimits limits = accountLimitsService.lambdaQuery()
                .eq(AccountLimits::getAccountId, req.getFromAccountId())
                .one();
        
        if (limits != null) {
            if (transferAmount.compareTo(limits.getSingleTransferLimit()) > 0) {
                throw new Exception("单笔转账金额超过限额");
            }
            // 这里可以添加日累计限额校验
        }
        
        // 查询转入账户
        Accounts toAccount = accountsService.lambdaQuery()
                .eq(Accounts::getAccountNumber, req.getToAccountNumber())
                .one();
        
        // 判断转账类型: 1-行内转账, 2-跨行转账
        Integer transferType = (toAccount != null) ? 1 : 2;
        
        // 生成转账ID
        String transferId = "TRF" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        
        // 扣减转出账户余额
        accountsService.lambdaUpdate()
                .eq(Accounts::getId, fromAccount.getId())
                .set(Accounts::getBalance, fromAccount.getBalance().subtract(transferAmount))
                .set(Accounts::getAvailableBalance, fromAccount.getAvailableBalance().subtract(transferAmount))
                .update();
        
        // 如果是行内转账，增加转入账户余额
        if (toAccount != null) {
            accountsService.lambdaUpdate()
                    .eq(Accounts::getId, toAccount.getId())
                    .set(Accounts::getBalance, toAccount.getBalance().add(transferAmount))
                    .set(Accounts::getAvailableBalance, toAccount.getAvailableBalance().add(transferAmount))
                    .update();
        }
        
        // 创建转账记录
        Transfers transfer = new Transfers()
                .setTransferId(transferId)
                .setFromAccountId(fromAccount.getId())
                .setToAccountId(toAccount != null ? toAccount.getId() : null)
                .setFromAccountNumber(fromAccount.getAccountNumber())
                .setToAccountNumber(req.getToAccountNumber())
                .setToAccountName(req.getToAccountName())
                .setToBankName(req.getToBankName())
                .setAmount(transferAmount)
                .setFee(BigDecimal.ZERO) // 暂不收取手续费
                .setCurrency("CNY")
                .setTransferType(transferType)
                .setRemark(req.getRemark())
                .setStatus(1)
                .setTransferTime(LocalDateTime.now());

        save(transfer);
        
        // 记录转出交易
        transactionsService.save(new Transactions()
                .setTransactionId("TXN" + System.currentTimeMillis() + "OUT")
                .setAccountId(fromAccount.getId())
                .setTransactionType("TRANSFER_OUT")
                .setAmount(transferAmount.negate())
                .setBalanceAfter(fromAccount.getBalance().subtract(transferAmount))
                .setDescription("转账给" + req.getToAccountName())
                .setCounterpartyAccount(req.getToAccountNumber())
                .setCounterpartyName(req.getToAccountName())
                .setStatus(1)
                .setTransactionTime(LocalDateTime.now()));
        
        // 如果是行内转账，记录转入交易
        if (toAccount != null) {
            transactionsService.save(new Transactions()
                    .setTransactionId("TXN" + System.currentTimeMillis() + "IN")
                    .setAccountId(toAccount.getId())
                    .setTransactionType("TRANSFER_IN")
                    .setAmount(transferAmount)
                    .setBalanceAfter(toAccount.getBalance().add(transferAmount))
                    .setDescription("收到转账")
                    .setCounterpartyAccount(fromAccount.getAccountNumber())
                    .setCounterpartyName(fromAccount.getAccountNumber())
                    .setStatus(1)
                    .setTransactionTime(LocalDateTime.now()));
        }
        
        return transfer;
    }

    @Override
    public Transfers getTransferDetail(Long userId, String transferId) {
        // 验证用户权限
        Transfers transfer = lambdaQuery()
                .eq(Transfers::getTransferId, transferId)
                .one();
        
        if (transfer == null) {
            return null;
        }
        
        // 验证转账记录属于该用户
        Accounts account = accountsService.getById(transfer.getFromAccountId());
        if (account == null || !account.getUserId().equals(userId)) {
            return null;
        }
        
        return transfer;
    }
}
