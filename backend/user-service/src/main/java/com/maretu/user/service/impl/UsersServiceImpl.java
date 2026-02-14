package com.maretu.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maretu.common.dto.Context;
import com.maretu.common.utils.JwtUtils;
import com.maretu.user.enums.UserStatus;
import com.maretu.user.mapper.UsersMapper;
import com.maretu.user.pojo.Users;
import com.maretu.user.service.IUsersService;
import com.maretu.user.utils.HashUtil;
import com.maretu.user.utils.MailUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {

    private final MailUtil mailUtil;
    private final StringRedisTemplate stringRedisTemplate;

    public UsersServiceImpl(MailUtil mailUtil, StringRedisTemplate stringRedisTemplate) {
        this.mailUtil = mailUtil;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public String login(Users user, String ip) {
        Users userToLogin = lambdaQuery()
                .eq(user.getEmail() != null, Users::getEmail, user.getEmail())
                .or()
                .eq(user.getPhone() != null, Users::getPhone, user.getPhone())
                .one();
        if (userToLogin == null || !HashUtil.checkPassword(user.getPassword(), userToLogin.getPassword())) {
            throw new RuntimeException("email/phone or password not correct");
        }
        if (!Objects.equals(userToLogin.getStatus(), UserStatus.NORMAL.getCode())) {
            throw new RuntimeException("user's status is not active");
        }
        Context context = new Context();
        context.setUserId(Math.toIntExact(userToLogin.getId()))
                .setUsername(userToLogin.getUsername())
                .setEmail(userToLogin.getEmail());
        loginLog(userToLogin.setLastLoginIp(ip));
        return JwtUtils.generateJwt(context);
    }

    @Async("virtualThreadPoolExecutor")
    public void loginLog(Users user) {
        try {
            lambdaUpdate()
                    .eq(Users::getId, user.getId())
                    .set(Users::getLastLoginIp, user.getLastLoginIp())
                    .set(Users::getLastLoginTime, System.currentTimeMillis())
                    .update();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }
}
