package com.maretu.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maretu.user.pojo.Users;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
public interface IUsersService extends IService<Users> {

    String login(Users user, String ip);

    Boolean sendCode(Users user, String type);

    Boolean register(Users user, String verifyCode);
}
