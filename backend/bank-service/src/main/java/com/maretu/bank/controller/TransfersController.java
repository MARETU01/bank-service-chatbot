package com.maretu.bank.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maretu.bank.dto.TransferReq;
import com.maretu.bank.pojo.Transfers;
import com.maretu.bank.service.ITransfersService;
import com.maretu.common.dto.Context;
import com.maretu.common.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 转账记录表 前端控制器
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/transfers")
public class TransfersController {

    private final ITransfersService transfersService;
    private final ObjectMapper jacksonObjectMapper;

    /**
     * 执行转账
     */
    @PostMapping
    public Result<Transfers> transfer(@RequestHeader("user-info") String userJson,
                                      @RequestBody TransferReq req) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            return Result.success(transfersService.transfer(context.getUserId().longValue(), req));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    /**
     * 获取转账详情
     */
    @GetMapping("/{transferId}")
    public Result<Transfers> getTransferDetail(@RequestHeader("user-info") String userJson,
                                               @PathVariable("transferId") String transferId) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            Transfers transfer = transfersService.getTransferDetail(context.getUserId().longValue(), transferId);
            if (transfer == null) {
                return Result.failure("转账记录不存在或无权限");
            }
            return Result.success(transfer);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}
