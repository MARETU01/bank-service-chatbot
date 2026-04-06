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
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

    /**
     * Lua脚本：原子性地读取List并自动续期
     * 如果key存在且List非空，则刷新TTL并返回数据；否则返回空列表
     */
    @SuppressWarnings("unchecked")
    private static final DefaultRedisScript<List<String>> GET_AND_RENEW_SCRIPT;
    static {
        GET_AND_RENEW_SCRIPT = new DefaultRedisScript<>();
        GET_AND_RENEW_SCRIPT.setScriptText(
                "local data = redis.call('LRANGE', KEYS[1], 0, -1) " +
                "if #data > 0 then " +
                "  redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
                "end " +
                "return data"
        );
        GET_AND_RENEW_SCRIPT.setResultType((Class) List.class);
    }

    /**
     * Lua脚本：原子性地写入List并设置TTL（先清除旧数据再写入）
     * 保证RPUSH和EXPIRE在同一原子操作中完成，避免出现永不过期的缓存
     */
    private static final DefaultRedisScript<Long> PUSH_LIST_WITH_TTL_SCRIPT;
    static {
        PUSH_LIST_WITH_TTL_SCRIPT = new DefaultRedisScript<>();
        PUSH_LIST_WITH_TTL_SCRIPT.setScriptText(
                "redis.call('DEL', KEYS[1]) " +
                "for i = 2, #ARGV do " +
                "  redis.call('RPUSH', KEYS[1], ARGV[i]) " +
                "end " +
                "redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
                "return 1"
        );
        PUSH_LIST_WITH_TTL_SCRIPT.setResultType(Long.class);
    }

    @Override
    public List<String> getUserRoles(Integer userId) {
        String cacheKey = RedisConstants.USER_ROLES_KEY + userId;
        String ttlSeconds = String.valueOf(RedisConstants.USER_ROLES_TTL * 60);

        // 1. 使用Lua脚本原子性地读取缓存并自动续期
        List<String> cachedRoles = stringRedisTemplate.execute(
                GET_AND_RENEW_SCRIPT,
                List.of(cacheKey),
                ttlSeconds
        );
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

        // 5. 使用Lua脚本原子性地写入缓存并设置TTL，避免RPUSH和EXPIRE之间崩溃导致缓存永不过期
        if (!roleCodes.isEmpty()) {
            String[] args = new String[roleCodes.size() + 1];
            args[0] = ttlSeconds;
            for (int i = 0; i < roleCodes.size(); i++) {
                args[i + 1] = roleCodes.get(i);
            }
            stringRedisTemplate.execute(PUSH_LIST_WITH_TTL_SCRIPT, List.of(cacheKey), (Object[]) args);
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
            throw new RuntimeException("Insufficient permissions: admin role required");
        }

        // Validate parameters
        if (roleCodes == null || roleCodes.isEmpty()) {
            throw new RuntimeException("At least one role must be assigned");
        }

        // 3. 校验角色代码是否合法
        List<String> validCodes = List.of(
                RoleCode.USER.getCode(),
                RoleCode.ADMIN.getCode(),
                RoleCode.CUSTOMER_SERVICE.getCode()
        );
        roleCodes.forEach(code -> {
            if (!validCodes.contains(code)) {
                throw new RuntimeException("Invalid role code: " + code);
            }
        });

        // Prevent admin from removing their own ADMIN role
        if (Objects.equals(Long.valueOf(adminUserId), userId)
                && !roleCodes.contains(RoleCode.ADMIN.getCode())) {
            throw new RuntimeException("Cannot remove your own admin role");
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
