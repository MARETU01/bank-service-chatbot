package com.maretu.user.service;

import com.maretu.user.pojo.UserRoles;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户角色关联表 服务类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
public interface IUserRolesService extends IService<UserRoles> {

    List<String> getUserRoles(Integer userId);
}
