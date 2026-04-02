package com.maretu.user.service.impl;

import com.maretu.user.enums.RoleCode;
import com.maretu.user.pojo.Roles;
import com.maretu.user.pojo.UserRoles;
import com.maretu.user.mapper.UserRolesMapper;
import com.maretu.user.service.IRolesService;
import com.maretu.user.service.IUserRolesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户角色关联表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Service
@RequiredArgsConstructor
public class UserRolesServiceImpl extends ServiceImpl<UserRolesMapper, UserRoles> implements IUserRolesService {

    private final IRolesService rolesService;

    @Override
    public List<String> getUserRoles(Integer userId) {
        // 1. 查询用户的角色关联记录
        List<UserRoles> userRoles = lambdaQuery()
                .eq(UserRoles::getUserId, userId)
                .list();

        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 提取角色ID列表
        List<Long> roleIds = userRoles.stream()
                .map(UserRoles::getRoleId)
                .collect(Collectors.toList());

        // 3. 查询角色信息，返回角色代码列表
        return rolesService.lambdaQuery()
                .in(Roles::getId, roleIds)
                .list()
                .stream()
                .map(Roles::getRoleCode)
                .collect(Collectors.toList());
    }

    @Override
    @Async("virtualThreadPoolExecutor")
    public void assignDefaultRole(Long userId) {
        Roles userRole = rolesService.lambdaQuery()
                .eq(Roles::getRoleCode, RoleCode.USER.getCode())
                .one();
        if (userRole != null) {
            UserRoles ur = new UserRoles();
            ur.setUserId(userId)
                    .setRoleId(userRole.getId());
            save(ur);
        }
    }
}
