package com.maretu.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maretu.user.pojo.UserRoles;

/**
 * 用户角色关联服务类
 *
 * @author maretu
 * @since 2025-04-23
 */
public interface IUserRolesService extends IService<UserRoles> {

    /**
     * 为用户分配角色
     */
    boolean assignRole(Long userId, Long roleId);

    /**
     * 移除用户的角色
     */
    boolean removeRole(Long userId, Long roleId);

    /**
     * 检查用户是否拥有指定角色
     */
    boolean hasRole(Long userId, String roleCode);
}
