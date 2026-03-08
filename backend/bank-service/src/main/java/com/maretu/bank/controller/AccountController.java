package com.maretu.bank.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maretu.bank.dto.DashboardStats;
import com.maretu.bank.pojo.Accounts;
import com.maretu.bank.service.IAccountsService;
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
