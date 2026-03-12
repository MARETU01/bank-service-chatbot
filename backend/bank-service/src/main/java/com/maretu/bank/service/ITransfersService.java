package com.maretu.bank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maretu.bank.dto.TransferReq;
import com.maretu.bank.pojo.Transfers;

import java.util.List;

/**
 * <p>
 * 转账记录表 服务类
 * </p>
 *
 * @author maretu
 * @since 2026-03-07
 */
public interface ITransfersService extends IService<Transfers> {

    /**
     * 执行转账
     */
    Transfers executeTransfer(String userJson, TransferReq req);

    /**
     * 根据账户ID获取转账记录
     */
    List<Transfers> getTransfersByAccountId(Long accountId, Integer page, Integer size);

    /**
     * 根据多个账户ID获取转账记录
     */
    List<Transfers> getTransfersByAccountIds(List<Long> accountIds, Integer page, Integer size);
}
