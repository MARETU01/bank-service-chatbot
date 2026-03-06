package com.maretu.bank.service;

import com.maretu.bank.dto.DashboardStats;
import com.maretu.bank.pojo.Accounts;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 账户表 服务类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
public interface IAccountsService extends IService<Accounts> {

    /**
     * 根据用户ID获取账户列表
     */
    List<Accounts> getAccountsByUserId(Long userId);

    /**
     * 根据用户ID获取账户详情
     */
    Accounts getAccountDetail(Long userId, Long accountId);

    /**
     * 获取仪表盘统计数据
     */
    DashboardStats getDashboardStats(Long userId);
}
