package com.maretu.bank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maretu.bank.dto.DashboardStats;
import com.maretu.bank.pojo.Accounts;

import java.util.List;

/**
 * <p>
 * 账户表 服务类
 * </p>
 *
 * @author maretu
 * @since 2026-03-07
 */
public interface IAccountsService extends IService<Accounts> {

    /**
     * 根据用户ID获取账户列表
     */
    List<Accounts> getAccountsByUserId(Long userId);

    /**
     * 根据账户ID和用户ID获取账户（验证归属）
     */
    Accounts getAccountByIdAndUserId(Long accountId, Long userId);

    /**
     * 获取仪表盘统计数据
     */
    DashboardStats getDashboardStats(Long userId);
}
