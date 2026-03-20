package com.maretu.chat.config;

import com.maretu.api.client.BankClient;
import com.maretu.api.dto.AccountDTO;
import com.maretu.common.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FunctionCallTools {
    
    private final BankClient bankClient;

    @Tool(description = "查询用户的所有银行账户，包括账户号码、余额、账户类型等信息")
    public List<AccountDTO> getAllAccounts(ToolContext toolContext) {

        Result<List<AccountDTO>> result = bankClient.getAccounts(toolContext.getContext().get("context").toString());
        
        if (result == null || result.getCode().equals(Result.FAILURE_CODE) || result.getData() == null) {
            throw new RuntimeException("查询账户失败：" + (result != null ? result.getMessage() : "unknown error"));
        }
        
        return result.getData();
    }

}
