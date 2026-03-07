package com.maretu.bank.service.impl;

import com.maretu.bank.pojo.Transfers;
import com.maretu.bank.mapper.TransfersMapper;
import com.maretu.bank.service.ITransfersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 转账记录表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-03-07
 */
@Service
public class TransfersServiceImpl extends ServiceImpl<TransfersMapper, Transfers> implements ITransfersService {

}
