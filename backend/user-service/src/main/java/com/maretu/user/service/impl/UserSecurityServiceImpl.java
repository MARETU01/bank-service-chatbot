package com.maretu.user.service.impl;

import com.maretu.user.pojo.UserSecurity;
import com.maretu.user.mapper.UserSecurityMapper;
import com.maretu.user.service.IUserSecurityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maretu.user.utils.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * <p>
 * 用户支付密码表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@RequiredArgsConstructor
@Service
public class UserSecurityServiceImpl extends ServiceImpl<UserSecurityMapper, UserSecurity> implements IUserSecurityService {

    // 最大错误次数
    private static final int MAX_ATTEMPTS = 3;
    // 锁定时间（分钟）
    private static final int LOCK_MINUTES = 10;

    @Override
    @Transactional
    public void setPayPassword(Long userId, String payPassword) {
        if (userId == null || !StringUtils.hasText(payPassword)) {
            throw new RuntimeException("User ID and payment password cannot be empty");
        }

        // Check password length
        if (payPassword.length() != 6) {
            throw new RuntimeException("Payment password must be 6 digits");
        }

        UserSecurity userSecurity = lambdaQuery()
                .eq(UserSecurity::getUserId, userId)
                .one();
        String encodedPassword = HashUtil.encodePassword(payPassword);

        if (userSecurity == null) {
            // 新建记录
            userSecurity = new UserSecurity()
                    .setUserId(userId)
                    .setPayPassword(encodedPassword)
                    .setPayPasswordAttempts(0);
            save(userSecurity);
        } else {
            throw new RuntimeException("Payment password already set, please use update function to change password");
        }
    }

    @Override
    @Transactional
    public void updatePayPassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null || !StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
            throw new RuntimeException("User ID and password cannot be empty");
        }

        // Verify old password
        if (!verifyPayPassword(userId, oldPassword)) {
            throw new RuntimeException("Original payment password incorrect");
        }

        // 设置新密码
        setPayPassword(userId, newPassword);
    }

    @Override
    @Transactional
    public Boolean verifyPayPassword(Long userId, String payPassword) {
        if (userId == null || !StringUtils.hasText(payPassword)) {
            throw new RuntimeException("User ID and payment password cannot be empty");
        }

        UserSecurity userSecurity = lambdaQuery()
                .eq(UserSecurity::getUserId, userId)
                .one();
        if (userSecurity == null || !StringUtils.hasText(userSecurity.getPayPassword())) {
            throw new RuntimeException("Please set payment password first");
        }

        // Check if locked
        if (userSecurity.getPayPasswordLockedUntil() != null 
                && userSecurity.getPayPasswordLockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Payment password is locked, please try again after " + LOCK_MINUTES + " minutes");
        }

        // 验证密码
        boolean isCorrect = HashUtil.checkPassword(payPassword, userSecurity.getPayPassword());

        if (isCorrect) {
            // 密码正确，重置错误次数
            if (userSecurity.getPayPasswordAttempts() > 0 || userSecurity.getPayPasswordLockedUntil() != null) {
                userSecurity.setPayPasswordAttempts(0)
                        .setPayPasswordLockedUntil(null);
                updateById(userSecurity);
            }
        } else {
            // 密码错误，增加错误次数
            int attempts = (userSecurity.getPayPasswordAttempts() == null ? 0 : userSecurity.getPayPasswordAttempts()) + 1;
            userSecurity.setPayPasswordAttempts(attempts);

            if (attempts >= MAX_ATTEMPTS) {
                // Lock
                userSecurity.setPayPasswordLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
                updateById(userSecurity);
                throw new RuntimeException("Too many incorrect password attempts, locked for " + LOCK_MINUTES + " minutes");
            } else {
                updateById(userSecurity);
                throw new RuntimeException("Incorrect payment password, " + (MAX_ATTEMPTS - attempts) + " attempts remaining");
            }
        }

        return true;
    }

    @Override
    public Boolean hasPayPassword(Long userId) {
        if (userId == null) {
            return false;
        }
        UserSecurity userSecurity = lambdaQuery()
                .eq(UserSecurity::getUserId, userId)
                .one();
        return userSecurity != null && StringUtils.hasText(userSecurity.getPayPassword());
    }
}
