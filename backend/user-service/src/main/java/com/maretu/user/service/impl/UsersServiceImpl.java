package com.maretu.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maretu.user.mapper.UsersMapper;
import com.maretu.user.pojo.Users;
import com.maretu.user.service.IUsersService;
import com.maretu.user.utils.MailUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author
 * @since 2025-04-22
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {

    private final MailUtil mailUtil;
    private final StringRedisTemplate stringRedisTemplate;

    public UsersServiceImpl(MailUtil mailUtil, StringRedisTemplate stringRedisTemplate) {
        this.mailUtil = mailUtil;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public String login(Users user) {
    return null;
    }
}
