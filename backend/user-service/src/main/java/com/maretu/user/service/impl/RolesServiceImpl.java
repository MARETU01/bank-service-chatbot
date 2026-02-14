package com.maretu.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maretu.user.mapper.RolesMapper;
import com.maretu.user.mapper.UserRolesMapper;
import com.maretu.user.pojo.Roles;
import com.maretu.user.pojo.UserRoles;
import com.maretu.user.service.IRolesService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 角色服务实现类
 *
 * @author maretu
 * @since 2025-04-23
 */
@Service
public class RolesServiceImpl extends ServiceImpl<RolesMapper, Roles> implements IRolesService {

    private final UserRolesMapper userRolesMapper;

    public RolesServiceImpl(UserRolesMapper userRolesMapper) {
        this.userRolesMapper = userRolesMapper;
    }

    @Override
    public Roles getByCode(String roleCode) {
        return lambdaQuery().eq(Roles::getRoleCode, roleCode).one();
    }

    @Override
    public List<String> getRoleCodesByUserId(Long userId) {
        // 获取用户角色关联
        List<UserRoles> userRoles = userRolesMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRoles>()
                .eq(UserRoles::getUserId, userId)
        );
        
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取角色ID列表
        List<Long> roleIds = userRoles.stream()
                .map(UserRoles::getRoleId)
                .toList();

        // 获取角色信息
        List<Roles> roles = listByIds(roleIds);
        
        return roles.stream()
                .map(Roles::getRoleCode)
                .toList();
    }
}
