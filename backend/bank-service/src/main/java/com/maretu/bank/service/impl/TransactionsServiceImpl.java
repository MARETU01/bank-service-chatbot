package com.maretu.bank.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maretu.bank.dto.TransactionQueryReq;
import com.maretu.bank.pojo.Transactions;
import com.maretu.bank.mapper.TransactionsMapper;
import com.maretu.bank.service.IAccountsService;
import com.maretu.bank.service.ITransactionsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
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

    @Lazy
    @Autowired
    private IAccountsService accountsService;

    @Override
    public List<Transactions> queryTransactions(TransactionQueryReq req) {
        var query = lambdaQuery()
                .eq(req.getAccountId() != null && !req.getAllAccounts(), Transactions::getAccountId, req.getAccountId())
                .eq(StringUtils.hasText(req.getTransactionType()), Transactions::getTransactionType, req.getTransactionType())
                .eq(req.getStatus() != null, Transactions::getStatus, req.getStatus())
                .ge(StringUtils.hasText(req.getStartDate()), Transactions::getTransactionTime, 
                        StringUtils.hasText(req.getStartDate()) ? LocalDateTime.parse(req.getStartDate() + "T00:00:00") : null)
                .le(StringUtils.hasText(req.getEndDate()), Transactions::getTransactionTime, 
                        StringUtils.hasText(req.getEndDate()) ? LocalDateTime.parse(req.getEndDate() + "T23:59:59") : null);
        
        // 如果查询所有账户，先获取用户的所有账户 ID
        if (Boolean.TRUE.equals(req.getAllAccounts()) && req.getUserId() != null) {
            List<Long> accountIds = accountsService.getAccountIdsByUserId(req.getUserId());
            if (accountIds == null || accountIds.isEmpty()) {
                // 用户没有账户，返回空列表
                return List.of();
            }
            query.in(Transactions::getAccountId, accountIds);
        }
        
        return query.orderByDesc(Transactions::getTransactionTime)
                .page(new Page<>(req.getPage(), req.getSize()))
                .getRecords();
    }
}
