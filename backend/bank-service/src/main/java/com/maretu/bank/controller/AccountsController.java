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
 * <p>
 * 账户表 前端控制器
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/accounts")
public class AccountsController {

    private final IAccountsService accountsService;
    private final ObjectMapper jacksonObjectMapper;

    /**
     * 获取当前用户的账户列表
     */
    @GetMapping
    public Result<List<Accounts>> getAccounts(@RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        Long userId = context.getUserId().longValue();
        try {
            return Result.success(accountsService.getAccountsByUserId(userId));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 获取账户详情
     */
    @GetMapping("/{id}")
    public Result<Accounts> getAccountDetail(@RequestHeader("user-info") String userJson,
                                             @PathVariable("id") Long accountId) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            Accounts account = accountsService.getAccountDetail(context.getUserId().longValue(), accountId);
            if (account == null) {
                return Result.failure("账户不存在或无权限");
            }
            return Result.success(account);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/dashboard")
    public Result<DashboardStats> getDashboardStats(@RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            return Result.success(accountsService.getDashboardStats(context.getUserId().longValue()));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}
