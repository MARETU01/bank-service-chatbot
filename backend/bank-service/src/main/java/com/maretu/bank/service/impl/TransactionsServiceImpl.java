package com.maretu.bank.service.impl;

import com.maretu.bank.pojo.Transactions;
import com.maretu.bank.mapper.TransactionsMapper;
import com.maretu.bank.service.ITransactionsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 交易记录表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Service
public class TransactionsServiceImpl extends ServiceImpl<TransactionsMapper, Transactions> implements ITransactionsService {

}
