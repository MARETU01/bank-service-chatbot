package com.maretu.api.client;

import com.maretu.api.dto.AccountDTO;
import com.maretu.api.dto.DashboardStatsDTO;
import com.maretu.api.dto.TransactionDTO;
import com.maretu.api.dto.TransferDTO;
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

    @PostMapping("/accounts")
    Result<AccountDTO> createAccount(@RequestHeader("user-info") String userJson,
                                     @RequestBody AccountDTO req);

    @GetMapping("/accounts")
    Result<List<AccountDTO>> getAccounts(@RequestHeader("user-info") String userJson);

    @GetMapping("/accounts/{id}")
    Result<AccountDTO> getAccountDetail(@PathVariable("id") Long id,
                                        @RequestHeader("user-info") String userJson);

    @GetMapping("/accounts/dashboard/stats")
    Result<DashboardStatsDTO> getDashboardStats(@RequestHeader("user-info") String userJson);

    @PutMapping("/accounts/{id}")
    Result<AccountDTO> updateAccount(@PathVariable("id") Long id,
                                     @RequestHeader("user-info") String userJson,
                                     @RequestBody AccountDTO req);

    @PutMapping("/accounts/{id}/status")
    Result<AccountDTO> updateStatus(@PathVariable("id") Long id,
                                    @RequestHeader("user-info") String userJson,
                                    @RequestBody AccountDTO req);

    @GetMapping("/transactions")
    Result<List<TransactionDTO>> getTransactions(@RequestHeader("user-info") String userJson,
                                                  @RequestParam(value = "accountId", required = false) Long accountId,
                                                  @RequestParam(value = "type", required = false) String type,
                                                  @RequestParam(value = "status", required = false) Integer status,
                                                  @RequestParam(value = "startDate", required = false) String startDate,
                                                  @RequestParam(value = "endDate", required = false) String endDate,
                                                  @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                  @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                  @RequestParam(value = "allAccounts", defaultValue = "false") Boolean allAccounts);

    @GetMapping("/transfers")
    Result<List<TransferDTO>> getTransfers(@RequestHeader("user-info") String userJson,
                                           @RequestParam(value = "accountId", required = false) Long accountId,
                                           @RequestParam(value = "page", defaultValue = "1") Integer page,
                                           @RequestParam(value = "size", defaultValue = "10") Integer size);

    @PostMapping("/transfers")
    Result<TransferDTO> transfer(@RequestHeader("user-info") String userJson,
                                 @RequestBody TransferReqDTO req);
}
