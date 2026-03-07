package com.maretu.user.service.impl;

import com.maretu.common.dto.Context;
import com.maretu.common.utils.DesensitizeUtils;
import com.maretu.common.utils.JwtUtils;
import com.maretu.common.utils.RedisConstants;
import com.maretu.user.dto.ResetPasswordReq;
import com.maretu.user.dto.UpdateProfileReq;
import com.maretu.user.pojo.Users;
import com.maretu.user.mapper.UsersMapper;
import com.maretu.user.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maretu.user.utils.HashUtil;
import com.maretu.user.utils.MailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;

/**
 * 用户信息表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@RequiredArgsConstructor
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {

    private final StringRedisTemplate stringRedisTemplate;
    private final MailUtil mailUtil;
    @Lazy
    @Autowired
    private UsersServiceImpl self;

    @Override
    public String login(Users user, String ip) {
        String email = user.getEmail();
        String phone = user.getPhone();
        if (!StringUtils.hasText(email) && !StringUtils.hasText(phone)) {
            throw new RuntimeException("email or phone is required");
        }

        Users userToLogin = lambdaQuery()
                .and(q -> q.eq(StringUtils.hasText(email), Users::getEmail, email)
                .or()
                .eq(StringUtils.hasText(phone), Users::getPhone, phone))
                .one();

        if (userToLogin == null || !HashUtil.checkPassword(user.getPassword(), userToLogin.getPassword())) {
            throw new RuntimeException("email/phone or password not correct");
        }
        if (userToLogin.getStatus() != 1) {
            throw new RuntimeException("user's status is not active");
        }
        Context token = new Context();
        token.setUserId(Math.toIntExact(userToLogin.getId()))
                .setUsername(userToLogin.getUsername())
                .setEmail(userToLogin.getEmail())
                .setIp(ip);
        self.loginLog(userToLogin, ip);
        return JwtUtils.generateJwt(token);
    }

    @Override
    public Boolean sendCode(Users user, String type) {
        int verificationCode = ThreadLocalRandom.current().nextInt(100000, 1000000);
        String key;

        long ttl = switch (type) {
            case "register" -> {
                key = RedisConstants.VERIFY_CODE_KEY + user.getEmail();
                yield RedisConstants.VERIFY_CODE_TTL;
            }
            case "reset-password" -> {
                key = RedisConstants.RESET_PASSWORD_KEY + user.getEmail();
                yield RedisConstants.RESET_PASSWORD_TTL;
            }
            default -> throw new RuntimeException("unsupported type: " + type);
        };

        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(
                key,
                String.valueOf(verificationCode),
                ttl,
                TimeUnit.MINUTES
        );
        if (!Boolean.TRUE.equals(ok)) {
            throw new RuntimeException("verification code already sent, please try later");
        }

        self.sendEmail(user.getEmail(), String.valueOf(verificationCode), key);
        return true;
    }

    @Override
    public Boolean register(Users user, String verifyCode) {
        if (lambdaQuery().eq(Users::getEmail, user.getEmail()).one() != null) {
            throw new RuntimeException("email already registered");
        } else if (lambdaQuery().eq(Users::getUsername, user.getUsername()).one() != null) {
            throw new RuntimeException("username already registered");
        }
        String storedCode = stringRedisTemplate.opsForValue().get(
                RedisConstants.VERIFY_CODE_KEY + user.getEmail()
        );
        if (!Objects.equals(storedCode, verifyCode)) {
            throw new RuntimeException("verification code not correct");
        }
        String encodedPassword = HashUtil.encodePassword(user.getPassword());
        user.setPassword(encodedPassword)
                .setEmailVerified(true);
        save(user);
        stringRedisTemplate.delete(RedisConstants.VERIFY_CODE_KEY + user.getEmail());
        return true;
    }

    @Override
    public Boolean resetPassword(ResetPasswordReq req, String verifyCode) {
        if (req == null) {
            throw new RuntimeException("request is required");
        }
        if (!StringUtils.hasText(req.getEmail())) {
            throw new RuntimeException("email is required");
        }
        if (!StringUtils.hasText(verifyCode)) {
            throw new RuntimeException("verifyCode is required");
        }
        if (!StringUtils.hasText(req.getNewPassword())) {
            throw new RuntimeException("newPassword is required");
        }

        Users userInfo = lambdaQuery().eq(Users::getEmail, req.getEmail()).one();
        if (userInfo == null) {
            throw new RuntimeException("user not found");
        }
        if (userInfo.getStatus() != 1) {
            throw new RuntimeException("user's status is not active");
        }

        String storedCode = stringRedisTemplate.opsForValue().get(
                RedisConstants.RESET_PASSWORD_KEY + req.getEmail()
        );
        if (!Objects.equals(storedCode, verifyCode)) {
            throw new RuntimeException("verification code not correct");
        }

        String encodedPassword = HashUtil.encodePassword(req.getNewPassword());
        userInfo.setPassword(encodedPassword);
        updateById(userInfo);
        stringRedisTemplate.delete(RedisConstants.RESET_PASSWORD_KEY + req.getEmail());
        return true;
    }

    @Override
    public String refresh(Integer userId, String ip) {
        Users userInfo = lambdaQuery().eq(Users::getId, userId).one();
        if (userInfo == null) {
            throw new RuntimeException("user not found");
        }
        if (userInfo.getStatus() != 1) {
            throw new RuntimeException("user's status is not active");
        }
        Context token = new Context();
        token.setUserId(Math.toIntExact(userInfo.getId()))
                .setUsername(userInfo.getUsername())
                .setEmail(userInfo.getEmail())
                .setIp(ip);
        self.loginLog(userInfo, ip);
        return JwtUtils.generateJwt(token);
    }

    @Override
    public Users getCurrentUser(Integer userId) {
        Users userInfo = lambdaQuery().eq(Users::getId, userId).one();
        if (userInfo == null) {
            throw new RuntimeException("user not found");
        }
        return maskUserForResponse(userInfo);
    }

    @Override
    public Users updateProfile(Integer currentUserId, UpdateProfileReq req) {
        if (currentUserId == null) {
            throw new RuntimeException("user not found");
        }

        Users userInfo = lambdaQuery().eq(Users::getId, currentUserId).one();
        if (userInfo == null) {
            throw new RuntimeException("user not found");
        }
        if (userInfo.getStatus() != 1) {
            throw new RuntimeException("user's status is not active");
        }

        boolean needUpdate = false;

        if (StringUtils.hasText(req.getUsername()) && !Objects.equals(req.getUsername(), userInfo.getUsername())) {
            // 查重（排除自己）
            Users existing = lambdaQuery().eq(Users::getUsername, req.getUsername()).one();
            if (existing != null && !Objects.equals(existing.getId(), userInfo.getId())) {
                throw new RuntimeException("username already registered");
            }
            userInfo.setUsername(req.getUsername());
            needUpdate = true;
        }

        if (StringUtils.hasText(req.getPhone()) && !Objects.equals(req.getPhone(), userInfo.getPhone())) {
            Users existing = lambdaQuery().eq(Users::getPhone, req.getPhone()).one();
            if (existing != null && !Objects.equals(existing.getId(), userInfo.getId())) {
                throw new RuntimeException("phone already registered");
            }
            userInfo.setPhone(req.getPhone());
            needUpdate = true;
        }

        if (StringUtils.hasText(req.getRealName()) && !Objects.equals(req.getRealName(), userInfo.getRealName())) {
            userInfo.setRealName(req.getRealName());
            needUpdate = true;
        }

        if (!needUpdate) {
            return maskUserForResponse(userInfo);
        }

        updateById(userInfo);

        Users updated = lambdaQuery().eq(Users::getId, userInfo.getId()).one();
        return maskUserForResponse(updated);
    }

    @Async("virtualThreadPoolExecutor")
    public void sendEmail(String email, String code, String key) {
        try {
            mailUtil.sendVerificationCodeMail(email, code);
        } catch (Exception e) {
            stringRedisTemplate.delete(key);
        }
    }

    @Async("virtualThreadPoolExecutor")
    public void loginLog(Users user, String ip) {
        user.setLastLoginIp(ip)
                .setLastLoginTime(LocalDateTime.now());
        updateById(user);
    }

    private Users maskUserForResponse(Users user) {
        if (user == null) {
            return null;
        }
        return user.setPassword(null)
                .setRealName(DesensitizeUtils.maskRealName(user.getRealName()))
                .setPhone(DesensitizeUtils.maskPhone(user.getPhone()))
                .setEmail(DesensitizeUtils.maskEmail(user.getEmail()));
    }
}
