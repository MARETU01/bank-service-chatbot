package com.maretu.bank.service.impl;

import com.maretu.bank.pojo.AccountLimits;
import com.maretu.bank.mapper.AccountLimitsMapper;
import com.maretu.bank.service.IAccountLimitsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 账户限额表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Service
public class AccountLimitsServiceImpl extends ServiceImpl<AccountLimitsMapper, AccountLimits> implements IAccountLimitsService {

}
