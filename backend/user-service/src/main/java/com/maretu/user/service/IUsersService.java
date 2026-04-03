package com.maretu.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maretu.common.dto.PageDTO;
import com.maretu.user.dto.AdminUserQuery;
import com.maretu.user.dto.ResetPasswordReq;
import com.maretu.user.dto.UpdateProfileReq;
import com.maretu.user.pojo.Users;
import com.maretu.user.vo.AdminUserVO;

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

    Boolean resetPassword(ResetPasswordReq req, String verifyCode);

    String refresh(Integer userId, String ip);

    Users getCurrentUser(Integer userId);

    Users updateProfile(Integer currentUserId, UpdateProfileReq req);

    PageDTO<AdminUserVO> getAdminUserList(AdminUserQuery query, Integer adminUserId);

    Boolean toggleUserStatus(Long userId, Integer adminUserId);
}
