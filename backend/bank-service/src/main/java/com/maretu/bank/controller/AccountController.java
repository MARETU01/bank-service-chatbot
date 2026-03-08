package com.maretu.bank.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maretu.bank.dto.DashboardStats;
import com.maretu.bank.dto.TransactionQueryReq;
import com.maretu.bank.dto.TransferReq;
import com.maretu.bank.pojo.Accounts;
import com.maretu.bank.pojo.Transactions;
import com.maretu.bank.pojo.Transfers;
import com.maretu.bank.service.IAccountsService;
import com.maretu.bank.service.ITransactionsService;
import com.maretu.bank.service.ITransfersService;
import com.maretu.common.dto.Context;
import com.maretu.common.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 银行账户控制器
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final IAccountsService accountsService;
    private final ITransactionsService transactionsService;
    private final ITransfersService transfersService;
    private final ObjectMapper jacksonObjectMapper;

    /**
     * 创建新账户
     */
    @PostMapping
    public Result<Accounts> createAccount(@RequestHeader("user-info") String userJson,
                                          @RequestBody Accounts req) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            Accounts account = accountsService.createAccount(context.getUserId().longValue(), req);
            return Result.success(account);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 获取当前用户的所有账户
     */
    @GetMapping
    public Result<List<Accounts>> getAccounts(@RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            return Result.success(accountsService.getAccountsByUserId(context.getUserId().longValue()));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 获取账户详情
     */
    @GetMapping("/{id}")
    public Result<Accounts> getAccountDetail(@PathVariable("id") Long id,
                                             @RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            Accounts account = accountsService.getAccountByIdAndUserId(id, context.getUserId().longValue());
            if (account == null) {
                return Result.failure("账户不存在或无权访问");
            }
            return Result.success(account);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 获取交易记录
     */
    @GetMapping("/{accountId}/transactions")
    public Result<List<Transactions>> getTransactions(@PathVariable("accountId") Long accountId,
                                                      @RequestHeader("user-info") String userJson,
                                                      @RequestParam(value = "type", required = false) String type,
                                                      @RequestParam(value = "status", required = false) Integer status,
                                                      @RequestParam(value = "startDate", required = false) String startDate,
                                                      @RequestParam(value = "endDate", required = false) String endDate,
                                                      @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                      @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                      @RequestParam(value = "allAccounts", defaultValue = "false") Boolean allAccounts) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            if (!allAccounts) {
                // 验证账户归属
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
            if (allAccounts) {
                req.setUserId(context.getUserId().longValue());
            }
            
            return Result.success(transactionsService.queryTransactions(req));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 获取转账记录
     */
    @GetMapping("/{accountId}/transfers")
    public Result<List<Transfers>> getTransfers(@PathVariable("accountId") Long accountId,
                                                @RequestHeader("user-info") String userJson,
                                                @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                @RequestParam(value = "size", defaultValue = "10") Integer size) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            // 验证账户归属
            Accounts account = accountsService.getAccountByIdAndUserId(accountId, context.getUserId().longValue());
            if (account == null) {
                return Result.failure("账户不存在或无权访问");
            }
            
            return Result.success(transfersService.getTransfersByAccountId(accountId, page, size));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 发起转账
     */
    @PostMapping("/transfer")
    public Result<Transfers> transfer(@RequestHeader("user-info") String userJson,
                                      @RequestBody TransferReq req) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            // 验证账户归属
            Accounts account = accountsService.getAccountByIdAndUserId(req.getFromAccountId(), context.getUserId().longValue());
            if (account == null) {
                return Result.failure("账户不存在或无权访问");
            }
            
            Transfers transfer = transfersService.executeTransfer(req);
            return Result.success(transfer);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/dashboard/stats")
    public Result<DashboardStats> getDashboardStats(@RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            return Result.success(accountsService.getDashboardStats(context.getUserId().longValue()));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 更新账户信息
     */
    @PutMapping("/{id}")
    public Result<Accounts> updateAccount(@PathVariable("id") Long id,
                                          @RequestHeader("user-info") String userJson,
                                          @RequestBody Accounts req) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            Accounts account = accountsService.updateAccount(id, context.getUserId().longValue(), req);
            return Result.success(account);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 更新账户状态（冻结/解冻）
     */
    @PutMapping("/{id}/status")
    public Result<Accounts> updateStatus(@PathVariable("id") Long id,
                                         @RequestHeader("user-info") String userJson,
                                         @RequestBody Accounts req) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            Accounts account = accountsService.updateStatus(id, context.getUserId().longValue(), req.getStatus());
            return Result.success(account);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}
