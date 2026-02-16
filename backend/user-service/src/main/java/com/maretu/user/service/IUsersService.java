package com.maretu.user.service;

import com.maretu.user.pojo.Users;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
