package com.maretu.bank.service.impl;

import com.maretu.bank.pojo.Accounts;
import com.maretu.bank.mapper.AccountsMapper;
import com.maretu.bank.service.IAccountsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 账户表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-03-07
 */
@Service
public class AccountsServiceImpl extends ServiceImpl<AccountsMapper, Accounts> implements IAccountsService {

}
