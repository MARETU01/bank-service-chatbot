package com.maretu.bank.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maretu.bank.dto.TransferReq;
import com.maretu.bank.pojo.Accounts;
import com.maretu.bank.pojo.Transfers;
import com.maretu.bank.service.IAccountsService;
import com.maretu.bank.service.ITransfersService;
import com.maretu.common.dto.Context;
import com.maretu.common.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 转账控制器
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final IAccountsService accountsService;
    private final ITransfersService transfersService;
    private final ObjectMapper jacksonObjectMapper;

    /**
     * 获取转账记录
     * @param accountId 账户ID（可选，不传则查询所有账户）
     * @param page 页码
     * @param size 每页大小
     */
    @GetMapping
    public Result<List<Transfers>> getTransfers(
            @RequestHeader("user-info") String userJson,
            @RequestParam(value = "accountId", required = false) Long accountId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            if (accountId != null) {
                // 验证账户归属
                Accounts account = accountsService.getAccountByIdAndUserId(accountId, context.getUserId().longValue());
                if (account == null) {
                    return Result.failure("账户不存在或无权访问");
                }
                return Result.success(transfersService.getTransfersByAccountId(accountId, page, size));
            } else {
                // 查询用户所有账户的转账记录
                List<Long> accountIds = accountsService.getAccountIdsByUserId(context.getUserId().longValue());
                return Result.success(transfersService.getTransfersByAccountIds(accountIds, page, size));
            }
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 发起转账
     */
    @PostMapping
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
}
