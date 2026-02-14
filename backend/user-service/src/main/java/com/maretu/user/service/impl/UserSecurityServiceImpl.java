package com.maretu.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maretu.user.mapper.UserSecurityMapper;
import com.maretu.user.pojo.UserSecurity;
import com.maretu.user.service.IUserSecurityService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户安全信息服务实现类
 *
 * @author maretu
 * @since 2025-04-23
 */
@Service
public class UserSecurityServiceImpl extends ServiceImpl<UserSecurityMapper, UserSecurity> implements IUserSecurityService {

    /**
     * 最大登录失败次数
     */
    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Override
    public UserSecurity getByUserId(Long userId) {
        return lambdaQuery().eq(UserSecurity::getUserId, userId).one();
    }

    @Override
    public void incrementFailedAttempts(Long userId) {
        UserSecurity security = getByUserId(userId);
        if (security == null) {
            security = new UserSecurity();
            security.setUserId(userId);
            security.setFailedLoginAttempts(1);
            security.setTwoFactorEnabled(false);
            save(security);
        } else {
            int attempts = security.getFailedLoginAttempts() == null ? 0 : security.getFailedLoginAttempts();
            security.setFailedLoginAttempts(attempts + 1);
            
            // 如果失败次数达到上限，锁定账户
            if (attempts + 1 >= MAX_FAILED_ATTEMPTS) {
                lockUser(userId, 30); // 锁定30分钟
            }
            
            updateById(security);
        }
    }

    @Override
    public void resetFailedAttempts(Long userId) {
        UserSecurity security = getByUserId(userId);
        if (security != null) {
            security.setFailedLoginAttempts(0);
            security.setLockedUntil(null);
            updateById(security);
        }
    }

    @Override
    public boolean isLocked(Long userId) {
        UserSecurity security = getByUserId(userId);
        if (security == null || security.getLockedUntil() == null) {
            return false;
        }
        return security.getLockedUntil().isAfter(LocalDateTime.now());
    }

    @Override
    public void lockUser(Long userId, int minutes) {
        UserSecurity security = getByUserId(userId);
        if (security == null) {
            security = new UserSecurity();
            security.setUserId(userId);
            security.setLockedUntil(LocalDateTime.now().plusMinutes(minutes));
            security.setFailedLoginAttempts(MAX_FAILED_ATTEMPTS);
            security.setTwoFactorEnabled(false);
            save(security);
        } else {
            security.setLockedUntil(LocalDateTime.now().plusMinutes(minutes));
            updateById(security);
        }
    }

    @Override
    public void unlockUser(Long userId) {
        UserSecurity security = getByUserId(userId);
        if (security != null) {
            security.setLockedUntil(null);
            security.setFailedLoginAttempts(0);
            updateById(security);
        }
    }

    @Override
    public boolean setTwoFactor(Long userId, boolean enabled, String secret) {
        UserSecurity security = getByUserId(userId);
        if (security == null) {
            security = new UserSecurity();
            security.setUserId(userId);
            security.setTwoFactorEnabled(enabled);
            security.setTwoFactorSecret(secret);
            security.setFailedLoginAttempts(0);
            return save(security);
        } else {
            security.setTwoFactorEnabled(enabled);
            security.setTwoFactorSecret(secret);
            return updateById(security);
        }
    }

    @Override
    public boolean verifySecurityAnswer(Long userId, String answer) {
        UserSecurity security = getByUserId(userId);
        if (security == null || security.getSecurityAnswer() == null) {
            return false;
        }
        return security.getSecurityAnswer().equalsIgnoreCase(answer);
    }
}
