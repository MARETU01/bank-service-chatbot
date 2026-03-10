package com.maretu.user.service;

import com.maretu.user.pojo.UserSecurity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户支付密码表 服务类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
public interface IUserSecurityService extends IService<UserSecurity> {

    void setPayPassword(Long userId, String payPassword);

    void updatePayPassword(Long userId, String oldPassword, String newPassword);

    boolean verifyPayPassword(Long userId, String payPassword);

    boolean hasPayPassword(Long userId);

    UserSecurity getByUserId(Long userId);
}
