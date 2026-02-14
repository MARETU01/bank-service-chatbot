package com.maretu.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maretu.user.pojo.Users;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2025-04-22
 */
public interface IUsersService extends IService<Users> {

    String login(Users user);
}
