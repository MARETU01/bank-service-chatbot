package com.maretu.user.service.impl;

import com.maretu.common.dto.Context;
import com.maretu.common.utils.JwtUtils;
import com.maretu.user.pojo.Users;
import com.maretu.user.mapper.UsersMapper;
import com.maretu.user.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maretu.user.utils.HashUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
}
