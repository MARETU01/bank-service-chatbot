package com.maretu.bank.service;

import com.maretu.bank.dto.TransferReq;
import com.maretu.bank.pojo.Transfers;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 转账记录表 服务类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
public interface ITransfersService extends IService<Transfers> {

    /**
     * 执行转账
     */
    Transfers transfer(Long userId, TransferReq req) throws Exception;

    /**
     * 获取转账记录
     */
    Transfers getTransferDetail(Long userId, String transferId);
}
