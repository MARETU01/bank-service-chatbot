package com.maretu.chat.config;

import com.maretu.api.client.BankClient;
import com.maretu.api.dto.AccountDTO;
import com.maretu.api.dto.DashboardStatsDTO;
import com.maretu.api.dto.TransactionDTO;
import com.maretu.api.dto.TransferDTO;
import com.maretu.api.dto.TransferReqDTO;
import com.maretu.common.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI 助手工具类 - 提供银行服务相关的工具方法供 AI 调用
 */
@Component
@RequiredArgsConstructor
public class FunctionCallTools {
    
    private final BankClient bankClient;

    @Tool(description = "创建新的银行账户，需要提供账户名称、初始余额、货币类型、日限额等信息")
    public AccountDTO createAccount(ToolContext toolContext,
                                    String accountName,
                                    BigDecimal initialBalance,
                                    String currency,
                                    BigDecimal dailyLimit) {
        AccountDTO req = new AccountDTO()
                .setAccountName(accountName)
                .setBalance(initialBalance)
                .setCurrency(currency)
                .setDailyLimit(dailyLimit);
        
        Result<AccountDTO> result = bankClient.createAccount(toolContext.getContext().get("context").toString(), req);
        
        if (result == null || result.getCode().equals(Result.FAILURE_CODE) || result.getData() == null) {
            throw new RuntimeException("创建账户失败：" + (result != null ? result.getMessage() : "unknown error"));
        }
        
        return result.getData();
    }

    @Tool(description = "查询用户的所有银行账户，包括账户号码、余额、账户类型等信息")
    public List<AccountDTO> getAllAccounts(ToolContext toolContext) {
        Result<List<AccountDTO>> result = bankClient.getAccounts(toolContext.getContext().get("context").toString());
        
        if (result == null || result.getCode().equals(Result.FAILURE_CODE) || result.getData() == null) {
            throw new RuntimeException("查询账户失败：" + (result != null ? result.getMessage() : "unknown error"));
        }
        
        return result.getData();
    }

    @Tool(description = "查询指定账户的详细信息，包括账户 ID、账号、余额、状态等")
    public AccountDTO getAccountDetail(ToolContext toolContext, Long accountId) {
        Result<AccountDTO> result = bankClient.getAccountDetail(accountId, toolContext.getContext().get("context").toString());
        
        if (result == null || result.getCode().equals(Result.FAILURE_CODE) || result.getData() == null) {
            throw new RuntimeException("查询账户详情失败：" + (result != null ? result.getMessage() : "unknown error"));
        }
        
        return result.getData();
    }

    @Tool(description = "获取用户仪表盘统计数据，包括总资产、总收入、总支出、账户数量、交易笔数等")
    public DashboardStatsDTO getDashboardStats(ToolContext toolContext) {
        Result<DashboardStatsDTO> result = bankClient.getDashboardStats(toolContext.getContext().get("context").toString());
        
        if (result == null || result.getCode().equals(Result.FAILURE_CODE) || result.getData() == null) {
            throw new RuntimeException("获取统计数据失败：" + (result != null ? result.getMessage() : "unknown error"));
        }
        
        return result.getData();
    }

    @Tool(description = "更新账户信息，包括账户名称、余额、货币类型、日限额等")
    public AccountDTO updateAccount(ToolContext toolContext,
                                    Long accountId,
                                    String accountName,
                                    BigDecimal balance,
                                    String currency,
                                    BigDecimal dailyLimit) {
        AccountDTO req = new AccountDTO()
                .setAccountName(accountName)
                .setBalance(balance)
                .setCurrency(currency)
                .setDailyLimit(dailyLimit);
        
        Result<AccountDTO> result = bankClient.updateAccount(accountId, toolContext.getContext().get("context").toString(), req);
        
        if (result == null || result.getCode().equals(Result.FAILURE_CODE) || result.getData() == null) {
            throw new RuntimeException("更新账户失败：" + (result != null ? result.getMessage() : "unknown error"));
        }
        
        return result.getData();
    }

    @Tool(description = "更新账户状态，0-冻结，1-正常，2-关闭")
    public AccountDTO updateAccountStatus(ToolContext toolContext,
                                          Long accountId,
                                          Integer status) {
        AccountDTO req = new AccountDTO().setStatus(status);
        
        Result<AccountDTO> result = bankClient.updateStatus(accountId, toolContext.getContext().get("context").toString(), req);
        
        if (result == null || result.getCode().equals(Result.FAILURE_CODE) || result.getData() == null) {
            throw new RuntimeException("更新账户状态失败：" + (result != null ? result.getMessage() : "unknown error"));
        }
        
        return result.getData();
    }

    @Tool(description = "查询交易记录，可按账户 ID、交易类型、状态、日期范围筛选，支持分页")
    public List<TransactionDTO> getTransactions(ToolContext toolContext,
                                                Long accountId,
                                                String type,
                                                Integer status,
                                                String startDate,
                                                String endDate,
                                                Integer page,
                                                Integer size) {
        Result<List<TransactionDTO>> result = bankClient.getTransactions(
                toolContext.getContext().get("context").toString(),
                accountId,
                type,
                status,
                startDate,
                endDate,
                page != null ? page : 1,
                size != null ? size : 10,
                false
        );
        
        if (result == null || result.getCode().equals(Result.FAILURE_CODE) || result.getData() == null) {
            throw new RuntimeException("查询交易记录失败：" + (result != null ? result.getMessage() : "unknown error"));
        }
        
        return result.getData();
    }

    @Tool(description = "查询转账记录，可按账户 ID 筛选，支持分页")
    public List<TransferDTO> getTransfers(ToolContext toolContext,
                                          Long accountId,
                                          Integer page,
                                          Integer size) {
        Result<List<TransferDTO>> result = bankClient.getTransfers(
                toolContext.getContext().get("context").toString(),
                accountId,
                page != null ? page : 1,
                size != null ? size : 10
        );
        
        if (result == null || result.getCode().equals(Result.FAILURE_CODE) || result.getData() == null) {
            throw new RuntimeException("查询转账记录失败：" + (result != null ? result.getMessage() : "unknown error"));
        }
        
        return result.getData();
    }

    @Tool(description = "执行转账操作，需要提供转出账户 ID、转入账号、转入账户名称、转入银行、金额、备注和支付密码")
    public TransferDTO transfer(ToolContext toolContext,
                                Long fromAccountId,
                                String toAccountNumber,
                                String toAccountName,
                                String toBankName,
                                BigDecimal amount,
                                String remark,
                                String payPassword) {
        TransferReqDTO req = new TransferReqDTO()
                .setFromAccountId(fromAccountId)
                .setToAccountNumber(toAccountNumber)
                .setToAccountName(toAccountName)
                .setToBankName(toBankName)
                .setAmount(amount)
                .setRemark(remark)
                .setPayPassword(payPassword);
        
        Result<TransferDTO> result = bankClient.transfer(toolContext.getContext().get("context").toString(), req);
        
        if (result == null || result.getCode().equals(Result.FAILURE_CODE) || result.getData() == null) {
            throw new RuntimeException("转账失败：" + (result != null ? result.getMessage() : "unknown error"));
        }
        
        return result.getData();
    }

}
