package com.maretu.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maretu.user.pojo.UserSecurity;

/**
 * 用户安全信息服务类
 *
 * @author maretu
 * @since 2025-04-23
 */
public interface IUserSecurityService extends IService<UserSecurity> {

    /**
     * 根据用户ID获取安全信息
     */
    UserSecurity getByUserId(Long userId);

    /**
     * 增加登录失败次数
     */
    void incrementFailedAttempts(Long userId);

    /**
     * 重置登录失败次数
     */
    void resetFailedAttempts(Long userId);

    /**
     * 检查用户是否被锁定
     */
    boolean isLocked(Long userId);

    /**
     * 锁定用户
     */
    void lockUser(Long userId, int minutes);

    /**
     * 解锁用户
     */
    void unlockUser(Long userId);

    /**
     * 设置双因素认证
     */
    boolean setTwoFactor(Long userId, boolean enabled, String secret);

    /**
     * 验证安全问题
     */
    boolean verifySecurityAnswer(Long userId, String answer);
}
