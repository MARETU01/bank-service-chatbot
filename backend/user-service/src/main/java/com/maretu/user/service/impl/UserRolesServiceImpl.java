package com.maretu.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maretu.user.mapper.UserRolesMapper;
import com.maretu.user.pojo.Roles;
import com.maretu.user.pojo.UserRoles;
import com.maretu.user.service.IRolesService;
import com.maretu.user.service.IUserRolesService;
import org.springframework.stereotype.Service;

/**
 * 用户角色关联服务实现类
 *
 * @author maretu
 * @since 2025-04-23
 */
@Service
public class UserRolesServiceImpl extends ServiceImpl<UserRolesMapper, UserRoles> implements IUserRolesService {

    private final IRolesService rolesService;

    public UserRolesServiceImpl(IRolesService rolesService) {
        this.rolesService = rolesService;
    }

    @Override
    public boolean assignRole(Long userId, Long roleId) {
        // 检查是否已分配
        if (hasRole(userId, roleId)) {
            return true;
        }
        UserRoles userRoles = new UserRoles();
        userRoles.setUserId(userId);
        userRoles.setRoleId(roleId);
        return save(userRoles);
    }

    @Override
    public boolean removeRole(Long userId, Long roleId) {
        return lambdaUpdate()
                .eq(UserRoles::getUserId, userId)
                .eq(UserRoles::getRoleId, roleId)
                .remove();
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        Roles role = rolesService.getByCode(roleCode);
        if (role == null) {
            return false;
        }
        return hasRole(userId, role.getId());
    }

    /**
     * 检查用户是否拥有指定角色ID
     */
    private boolean hasRole(Long userId, Long roleId) {
        return lambdaQuery()
                .eq(UserRoles::getUserId, userId)
                .eq(UserRoles::getRoleId, roleId)
                .exists();
    }
}
