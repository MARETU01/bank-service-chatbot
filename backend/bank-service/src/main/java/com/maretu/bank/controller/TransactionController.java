package com.maretu.bank.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maretu.bank.dto.TransactionQueryReq;
import com.maretu.bank.pojo.Accounts;
import com.maretu.bank.pojo.Transactions;
import com.maretu.bank.service.IAccountsService;
import com.maretu.bank.service.ITransactionsService;
import com.maretu.common.dto.Context;
import com.maretu.common.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 交易记录控制器
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final IAccountsService accountsService;
    private final ITransactionsService transactionsService;
    private final ObjectMapper jacksonObjectMapper;

    /**
     * 获取交易记录
     * @param accountId 账户ID（可选，不传则查询所有账户）
     * @param type 交易类型
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param page 页码
     * @param size 每页大小
     * @param allAccounts 是否查询所有账户
     */
    @GetMapping
    public Result<List<Transactions>> getTransactions(
            @RequestHeader("user-info") String userJson,
            @RequestParam(value = "accountId", required = false) Long accountId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "allAccounts", defaultValue = "false") Boolean allAccounts) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            // 如果指定了账户ID且不是查询所有账户，验证账户归属
            if (accountId != null && !allAccounts) {
                Accounts account = accountsService.getAccountByIdAndUserId(accountId, context.getUserId().longValue());
                if (account == null) {
                    return Result.failure("账户不存在或无权访问");
                }
            }
            
            TransactionQueryReq req = new TransactionQueryReq()
                    .setAccountId(accountId)
                    .setTransactionType(type)
                    .setStatus(status)
                    .setStartDate(startDate)
                    .setEndDate(endDate)
                    .setPage(page)
                    .setSize(size)
                    .setAllAccounts(allAccounts);
            
            // 如果查询所有账户，传入 userId
            if (allAccounts || accountId == null) {
                req.setUserId(context.getUserId().longValue());
            }
            
            return Result.success(transactionsService.queryTransactions(req));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}
