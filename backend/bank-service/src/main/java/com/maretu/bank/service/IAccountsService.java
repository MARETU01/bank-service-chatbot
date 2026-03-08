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
     * 创建新账户
     */
    Accounts createAccount(Long userId, Accounts req);

    /**
     * 根据用户 ID 获取账户列表
     */
    List<Accounts> getAccountsByUserId(Long userId);

    /**
     * 根据用户 ID 获取账户 ID 列表
     */
    List<Long> getAccountIdsByUserId(Long userId);

    /**
     * 根据账户 ID 和用户 ID 获取账户（验证归属）
     */
    Accounts getAccountByIdAndUserId(Long accountId, Long userId);

    /**
     * 获取仪表盘统计数据
     */
    DashboardStats getDashboardStats(Long userId);

    /**
     * 更新账户信息
     */
    Accounts updateAccount(Long accountId, Long userId, Accounts req);

    /**
     * 更新账户状态（冻结/解冻/关闭）
     */
    Accounts updateStatus(Long accountId, Long userId, Integer status);
}
