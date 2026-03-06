package com.maretu.bank.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maretu.bank.dto.TransactionQueryReq;
import com.maretu.bank.pojo.Transactions;
import com.maretu.bank.service.ITransactionsService;
import com.maretu.common.dto.Context;
import com.maretu.common.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 交易记录表 前端控制器
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/transactions")
public class TransactionsController {

    private final ITransactionsService transactionsService;
    private final ObjectMapper jacksonObjectMapper;

    /**
     * 分页查询交易记录
     */
    @GetMapping
    public Result<Page<Transactions>> getTransactions(@RequestHeader("user-info") String userJson,
                                                      TransactionQueryReq req) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            return Result.success(transactionsService.queryTransactions(context.getUserId().longValue(), req));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 获取交易详情
     */
    @GetMapping("/{id}")
    public Result<Transactions> getTransactionDetail(@RequestHeader("user-info") String userJson,
                                                     @PathVariable("id") Long transactionId) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            Transactions transaction = transactionsService.getById(transactionId);
            if (transaction == null) {
                return Result.failure("交易记录不存在");
            }
            return Result.success(transaction);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}
