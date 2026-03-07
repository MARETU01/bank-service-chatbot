package com.maretu.api.client;

import com.maretu.api.dto.DashboardStatsDTO;
import com.maretu.api.dto.TransactionDTO;
import com.maretu.api.dto.TransferReqDTO;
import com.maretu.common.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 银行服务Feign客户端
 */
@FeignClient(value = "bank-service")
public interface BankClient {

    /**
     * 获取用户账户列表
     */
    @GetMapping("/accounts")
    Result<List<Object>> getAccounts(@RequestHeader("user-info") String userJson);

    /**
     * 获取账户详情
     */
    @GetMapping("/accounts/{id}")
    Result<Object> getAccountDetail(@PathVariable("id") Long id,
                                    @RequestHeader("user-info") String userJson);

    /**
     * 获取交易记录
     */
    @GetMapping("/accounts/{accountId}/transactions")
    Result<List<TransactionDTO>> getTransactions(@PathVariable("accountId") Long accountId,
                                                  @RequestHeader("user-info") String userJson,
                                                  @RequestParam(value = "type", required = false) String type,
                                                  @RequestParam(value = "status", required = false) Integer status,
                                                  @RequestParam(value = "startDate", required = false) String startDate,
                                                  @RequestParam(value = "endDate", required = false) String endDate,
                                                  @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                  @RequestParam(value = "size", defaultValue = "10") Integer size);

    /**
     * 获取转账记录
     */
    @GetMapping("/accounts/{accountId}/transfers")
    Result<List<Object>> getTransfers(@PathVariable("accountId") Long accountId,
                                      @RequestHeader("user-info") String userJson,
                                      @RequestParam(value = "page", defaultValue = "1") Integer page,
                                      @RequestParam(value = "size", defaultValue = "10") Integer size);

    /**
     * 发起转账
     */
    @PostMapping("/accounts/transfer")
    Result<Object> transfer(@RequestHeader("user-info") String userJson,
                            @RequestBody TransferReqDTO req);

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/accounts/dashboard/stats")
    Result<DashboardStatsDTO> getDashboardStats(@RequestHeader("user-info") String userJson);
}
