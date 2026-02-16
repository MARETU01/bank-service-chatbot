package com.maretu.user.service.impl;

import com.maretu.common.dto.Context;
import com.maretu.common.utils.JwtUtils;
import com.maretu.common.utils.RedisConstants;
import com.maretu.user.pojo.Users;
import com.maretu.user.mapper.UsersMapper;
import com.maretu.user.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maretu.user.utils.HashUtil;
import com.maretu.user.utils.MailUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {

    private final StringRedisTemplate stringRedisTemplate;
    private final MailUtil mailUtil;
    private final UsersServiceImpl self;

    public UsersServiceImpl(StringRedisTemplate stringRedisTemplate,
                            MailUtil mailUtil1,
                            @Lazy UsersServiceImpl self) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.mailUtil = mailUtil1;
        this.self = self;
    }

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
        return JwtUtils.generateJwt(token);
    }

    @Override
    public Boolean sendCode(Users user, String type) {
        try {
            int verificationCode = ThreadLocalRandom.current().nextInt(100000, 1000000);
            switch (type) {
                case "register":
                    stringRedisTemplate.opsForValue().set(
                            RedisConstants.VERIFY_CODE_KEY + user.getEmail(),
                            String.valueOf(verificationCode),
                            RedisConstants.VERIFY_CODE_TTL,
                            TimeUnit.MINUTES
                    );
                    break;
                case "reset-password":
                    stringRedisTemplate.opsForValue().set(
                            RedisConstants.RESET_PASSWORD_KEY + user.getEmail(),
                            String.valueOf(verificationCode),
                            RedisConstants.RESET_PASSWORD_TTL,
                            TimeUnit.MINUTES
                    );
                    break;
                default:
                    throw new RuntimeException("unsupported type: " + type);
            }
            self.sendEmail(user.getEmail(), String.valueOf(verificationCode));
        } catch (Exception e) {
            throw new RuntimeException("failed to send verification code");
        }
        return true;
    }

    @Async("virtualThreadPoolExecutor")
    public void sendEmail(String email, String code) {
        mailUtil.sendVerificationCodeMail(email, code);
    }
}
