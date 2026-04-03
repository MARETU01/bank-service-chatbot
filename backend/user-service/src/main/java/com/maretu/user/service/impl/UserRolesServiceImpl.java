package com.maretu.user.service.impl;

import com.maretu.common.utils.RedisConstants;
import com.maretu.user.enums.RoleCode;
import com.maretu.user.pojo.Roles;
import com.maretu.user.pojo.UserRoles;
import com.maretu.user.mapper.UserRolesMapper;
import com.maretu.user.service.IRolesService;
import com.maretu.user.service.IUserRolesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<String> getUserRoles(Integer userId) {
        String cacheKey = RedisConstants.USER_ROLES_KEY + userId;

        // 1. 先从缓存读取
        List<String> cachedRoles = stringRedisTemplate.opsForList().range(cacheKey, 0, -1);
        if (cachedRoles != null && !cachedRoles.isEmpty()) {
            return cachedRoles;
        }

        // 2. 缓存未命中，查询数据库
        List<UserRoles> userRoles = lambdaQuery()
                .eq(UserRoles::getUserId, userId)
                .list();

        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 提取角色ID列表
        List<Long> roleIds = userRoles.stream()
                .map(UserRoles::getRoleId)
                .collect(Collectors.toList());

        // 4. 从缓存获取角色信息，返回角色代码列表
        List<String> roleCodes = rolesService.getAllRoles().stream()
                .filter(role -> roleIds.contains(role.getId()))
                .map(Roles::getRoleCode)
                .collect(Collectors.toList());

        // 5. 写入缓存，设置30分钟TTL
        if (!roleCodes.isEmpty()) {
            stringRedisTemplate.opsForList().rightPushAll(cacheKey, roleCodes);
            stringRedisTemplate.expire(cacheKey, RedisConstants.USER_ROLES_TTL, TimeUnit.MINUTES);
        }

        return roleCodes;
    }

    @Override
    @Async("virtualThreadPoolExecutor")
    public void assignDefaultRole(Long userId) {
        // 从缓存获取角色列表，找到USER角色
        Roles userRole = rolesService.getAllRoles().stream()
                .filter(r -> RoleCode.USER.getCode().equals(r.getRoleCode()))
                .findFirst()
                .orElse(null);
        if (userRole != null) {
            UserRoles ur = new UserRoles();
            ur.setUserId(userId)
                    .setRoleId(userRole.getId());
            save(ur);
        }
    }

    @Override
    @Transactional
    public Boolean assignRoles(Long userId, List<String> roleCodes, Integer adminUserId) {
        // 1. 权限校验
        List<String> adminRoles = getUserRoles(adminUserId);
        if (adminRoles == null || !adminRoles.contains(RoleCode.ADMIN.getCode())) {
            throw new RuntimeException("权限不足：需要管理员角色");
        }

        // 2. 参数校验
        if (roleCodes == null || roleCodes.isEmpty()) {
            throw new RuntimeException("至少需要分配一个角色");
        }

        // 3. 校验角色代码是否合法
        List<String> validCodes = List.of(
                RoleCode.USER.getCode(),
                RoleCode.ADMIN.getCode(),
                RoleCode.CUSTOMER_SERVICE.getCode()
        );
        roleCodes.forEach(code -> {
            if (!validCodes.contains(code)) {
                throw new RuntimeException("无效的角色代码：" + code);
            }
        });

        // 4. 防止管理员移除自己的ADMIN角色
        if (Objects.equals(Long.valueOf(adminUserId), userId)
                && !roleCodes.contains(RoleCode.ADMIN.getCode())) {
            throw new RuntimeException("不能移除自己的管理员角色");
        }

        // 5. 删除该用户的所有旧角色
        lambdaUpdate()
                .eq(UserRoles::getUserId, userId)
                .remove();

        // 6. 批量插入新角色
        List<Roles> allRoles = rolesService.getAllRoles();
        List<UserRoles> newUserRoles = roleCodes.stream()
                .map(code -> allRoles.stream()
                        .filter(r -> r.getRoleCode().equals(code))
                        .findFirst()
                        .orElse(null))
                .filter(Objects::nonNull)
                .map(role -> {
                    UserRoles ur = new UserRoles();
                    ur.setUserId(userId)
                            .setRoleId(role.getId());
                    return ur;
                })
                .collect(Collectors.toList());

        saveBatch(newUserRoles);

        // 7. 清除该用户的角色缓存
        stringRedisTemplate.delete(RedisConstants.USER_ROLES_KEY + userId);

        return true;
    }

    @Override
    public List<Long> getUserIdsByRoleCode(String roleCode) {
        // 1. 找到角色ID
        Roles targetRole = rolesService.getAllRoles().stream()
                .filter(r -> r.getRoleCode().equals(roleCode))
                .findFirst()
                .orElse(null);

        if (targetRole == null) {
            return Collections.emptyList();
        }

        // 2. 查询拥有该角色的所有用户ID
        return lambdaQuery()
                .eq(UserRoles::getRoleId, targetRole.getId())
                .list()
                .stream()
                .map(UserRoles::getUserId)
                .collect(Collectors.toList());
    }
}
