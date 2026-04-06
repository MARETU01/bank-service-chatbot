package com.maretu.user.service.impl;

import com.maretu.common.dto.PageDTO;
import com.maretu.common.dto.Context;
import com.maretu.common.utils.JwtUtils;
import com.maretu.common.utils.RedisConstants;
import com.maretu.user.dto.AdminUserQuery;
import com.maretu.user.dto.ResetPasswordReq;
import com.maretu.user.dto.UpdateProfileReq;
import com.maretu.user.pojo.Users;
import com.maretu.user.mapper.UsersMapper;
import com.maretu.user.service.IUserRolesService;
import com.maretu.user.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maretu.user.enums.RoleCode;
import com.maretu.user.vo.AdminUserVO;
import com.maretu.user.utils.HashUtil;
import com.maretu.user.utils.MailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.Collections;

/**
 * 用户信息表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@RequiredArgsConstructor
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {

    private final StringRedisTemplate stringRedisTemplate;
    private final MailUtil mailUtil;
    private final IUserRolesService userRolesService;
    @Lazy
    @Autowired
    private UsersServiceImpl self;

    /**
     * Lua脚本：原子性地校验验证码并删除
     * 返回值：-1=key不存在, 0=验证码不匹配, 1=验证成功并已删除
     */
    private static final DefaultRedisScript<Long> VERIFY_AND_DELETE_SCRIPT;
    static {
        VERIFY_AND_DELETE_SCRIPT = new DefaultRedisScript<>();
        VERIFY_AND_DELETE_SCRIPT.setScriptText(
                "local stored = redis.call('GET', KEYS[1]) " +
                "if stored == false then return -1 end " +
                "if stored == ARGV[1] then " +
                "  redis.call('DEL', KEYS[1]) " +
                "  return 1 " +
                "else return 0 end"
        );
        VERIFY_AND_DELETE_SCRIPT.setResultType(Long.class);
    }

    @Override
    public String login(Users user, String ip) {
        Users userToLogin;
        String email = user.getEmail();
        String phone = user.getPhone();
        if (StringUtils.hasText(email)) {
            userToLogin = lambdaQuery()
                    .eq(Users::getEmail, email)
                    .one();
        } else  if (StringUtils.hasText(phone)) {
            userToLogin = lambdaQuery()
                    .eq(Users::getPhone, phone)
                    .one();
        } else {
            throw new RuntimeException("email or phone is required");
        }

        if (userToLogin == null || !HashUtil.checkPassword(user.getPassword(), userToLogin.getPassword())) {
            throw new RuntimeException("email/phone or password not correct");
        }
        if (userToLogin.getStatus() != 1) {
            throw new RuntimeException("user's status is not active");
        }
        Context token = new Context();
        token.setUserId(Math.toIntExact(userToLogin.getId()))
                .setUsername(userToLogin.getUsername())
                .setEmail(userToLogin.getEmail())
                .setIp(ip);
        self.loginLog(userToLogin, ip);
        return JwtUtils.generateJwt(token);
    }

    @Override
    public Boolean sendCode(Users user, String type) {
        int verificationCode = ThreadLocalRandom.current().nextInt(100000, 1000000);
        String key;

        long ttl = switch (type) {
            case "register" -> {
                key = RedisConstants.VERIFY_CODE_KEY + user.getEmail();
                yield RedisConstants.VERIFY_CODE_TTL;
            }
            case "reset-password" -> {
                key = RedisConstants.RESET_PASSWORD_KEY + user.getEmail();
                yield RedisConstants.RESET_PASSWORD_TTL;
            }
            default -> throw new RuntimeException("unsupported type: " + type);
        };

        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(
                key,
                String.valueOf(verificationCode),
                ttl,
                TimeUnit.MINUTES
        );
        if (!Boolean.TRUE.equals(ok)) {
            throw new RuntimeException("verification code already sent, please try later");
        }

        self.sendEmail(user.getEmail(), String.valueOf(verificationCode), key);
        return true;
    }

    @Override
    public Boolean register(Users user, String verifyCode) {
        if (lambdaQuery().eq(Users::getEmail, user.getEmail()).one() != null) {
            throw new RuntimeException("email already registered");
        } else if (lambdaQuery().eq(Users::getUsername, user.getUsername()).one() != null) {
            throw new RuntimeException("username already registered");
        }
        // 使用Lua脚本原子性地校验验证码并删除，防止并发重复消费
        String verifyKey = RedisConstants.VERIFY_CODE_KEY + user.getEmail();
        Long verifyResult = stringRedisTemplate.execute(
                VERIFY_AND_DELETE_SCRIPT,
                List.of(verifyKey),
                verifyCode
        );
        if (verifyResult == null || verifyResult != 1L) {
            throw new RuntimeException("verification code not correct");
        }
        String encodedPassword = HashUtil.encodePassword(user.getPassword());
        user.setPassword(encodedPassword)
                .setEmailVerified(true);
        save(user);
        userRolesService.assignDefaultRole(user.getId());
        return true;
    }

    @Override
    public Boolean resetPassword(ResetPasswordReq req, String verifyCode) {
        if (req == null) {
            throw new RuntimeException("request is required");
        }
        if (!StringUtils.hasText(req.getEmail())) {
            throw new RuntimeException("email is required");
        }
        if (!StringUtils.hasText(verifyCode)) {
            throw new RuntimeException("verifyCode is required");
        }
        if (!StringUtils.hasText(req.getNewPassword())) {
            throw new RuntimeException("newPassword is required");
        }

        Users userInfo = lambdaQuery().eq(Users::getEmail, req.getEmail()).one();
        if (userInfo == null) {
            throw new RuntimeException("user not found");
        }
        if (userInfo.getStatus() != 1) {
            throw new RuntimeException("user's status is not active");
        }

        // 使用Lua脚本原子性地校验验证码并删除，防止并发重复消费
        String resetKey = RedisConstants.RESET_PASSWORD_KEY + req.getEmail();
        Long verifyResult = stringRedisTemplate.execute(
                VERIFY_AND_DELETE_SCRIPT,
                List.of(resetKey),
                verifyCode
        );
        if (verifyResult == null || verifyResult != 1L) {
            throw new RuntimeException("verification code not correct");
        }

        String encodedPassword = HashUtil.encodePassword(req.getNewPassword());
        userInfo.setPassword(encodedPassword);
        updateById(userInfo);
        return true;
    }

    @Override
    public String refresh(Integer userId, String ip) {
        Users userInfo = lambdaQuery().eq(Users::getId, userId).one();
        if (userInfo == null) {
            throw new RuntimeException("user not found");
        }
        if (userInfo.getStatus() != 1) {
            throw new RuntimeException("user's status is not active");
        }
        Context token = new Context();
        token.setUserId(Math.toIntExact(userInfo.getId()))
                .setUsername(userInfo.getUsername())
                .setEmail(userInfo.getEmail())
                .setIp(ip);
        self.loginLog(userInfo, ip);
        return JwtUtils.generateJwt(token);
    }

    @Override
    public Users getCurrentUser(Integer userId) {
        Users userInfo = lambdaQuery().eq(Users::getId, userId).one();
        if (userInfo == null) {
            throw new RuntimeException("user not found");
        }
        return userInfo.setPassword(null);
    }

    @Override
    public Users updateProfile(Integer currentUserId, UpdateProfileReq req) {
        if (currentUserId == null) {
            throw new RuntimeException("user not found");
        }

        Users userInfo = lambdaQuery().eq(Users::getId, currentUserId).one();
        if (userInfo == null) {
            throw new RuntimeException("user not found");
        }
        if (userInfo.getStatus() != 1) {
            throw new RuntimeException("user's status is not active");
        }

        boolean needUpdate = false;

        if (StringUtils.hasText(req.getUsername()) && !Objects.equals(req.getUsername(), userInfo.getUsername())) {
            // 查重（排除自己）
            Users existing = lambdaQuery().eq(Users::getUsername, req.getUsername()).one();
            if (existing != null && !Objects.equals(existing.getId(), userInfo.getId())) {
                throw new RuntimeException("username already registered");
            }
            userInfo.setUsername(req.getUsername());
            needUpdate = true;
        }

        if (StringUtils.hasText(req.getPhone()) && !Objects.equals(req.getPhone(), userInfo.getPhone())) {
            Users existing = lambdaQuery().eq(Users::getPhone, req.getPhone()).one();
            if (existing != null && !Objects.equals(existing.getId(), userInfo.getId())) {
                throw new RuntimeException("phone already registered");
            }
            userInfo.setPhone(req.getPhone());
            needUpdate = true;
        }

        if (StringUtils.hasText(req.getRealName()) && !Objects.equals(req.getRealName(), userInfo.getRealName())) {
            userInfo.setRealName(req.getRealName());
            needUpdate = true;
        }

        if (!needUpdate) {
            return userInfo.setPassword(null);
        }

        updateById(userInfo);

        Users updated = lambdaQuery().eq(Users::getId, userInfo.getId()).one();
        return updated.setPassword(null);
    }

    @Async("virtualThreadPoolExecutor")
    public void sendEmail(String email, String code, String key) {
        try {
            mailUtil.sendVerificationCodeMail(email, code);
        } catch (Exception e) {
            stringRedisTemplate.delete(key);
        }
    }

    @Async("virtualThreadPoolExecutor")
    public void loginLog(Users user, String ip) {
        user.setLastLoginIp(ip)
                .setLastLoginTime(LocalDateTime.now());
        updateById(user);
    }

    @Override
    public PageDTO<AdminUserVO> getAdminUserList(AdminUserQuery query, Integer adminUserId) {
        // 1. 权限校验
        checkAdminRole(adminUserId);

        // 2. 构建分页查询
        Page<Users> page = new Page<>(query.getPage(), query.getSize());

        // 3. 如果有角色筛选，先查出拥有该角色的用户ID列表
        List<Long> filteredUserIds = null;
        if (query.getRole() != null && !query.getRole().isEmpty()) {
            filteredUserIds = userRolesService.getUserIdsByRoleCode(query.getRole());
            if (filteredUserIds.isEmpty()) {
                // 没有匹配的用户，直接返回空结果
                PageDTO<AdminUserVO> emptyResult = new PageDTO<>();
                emptyResult.setData(Collections.emptyList());
                emptyResult.setTotal(0L);
                emptyResult.setTotalPage(0L);
                return emptyResult;
            }
        }

        // 4. 构建查询条件
        List<Long> finalFilteredUserIds = filteredUserIds;
        Page<Users> usersPage = lambdaQuery()
                .and(query.getKeyword() != null && !query.getKeyword().isEmpty(),
                        q -> q.like(Users::getUsername, query.getKeyword())
                                .or()
                                .like(Users::getEmail, query.getKeyword()))
                .in(finalFilteredUserIds != null, Users::getId, finalFilteredUserIds)
                .orderByDesc(Users::getCreatedAt)
                .page(page);

        // 5. 转换为VO，组装角色信息
        List<AdminUserVO> voList = usersPage.getRecords().stream()
                .map(user -> {
                    AdminUserVO vo = new AdminUserVO();
                    vo.setId(user.getId());
                    vo.setUsername(user.getUsername());
                    vo.setEmail(user.getEmail());
                    vo.setActive(user.getStatus() != null && user.getStatus() == 1);
                    vo.setCreatedAt(user.getCreatedAt());
                    vo.setRoles(userRolesService.getUserRoles(Math.toIntExact(user.getId())));
                    return vo;
                })
                .collect(Collectors.toList());

        // 6. 组装分页结果
        PageDTO<AdminUserVO> result = new PageDTO<>();
        result.setData(voList);
        result.setTotal(usersPage.getTotal());
        result.setTotalPage(usersPage.getPages());
        return result;
    }

    @Override
    public Boolean toggleUserStatus(Long userId, Integer adminUserId) {
        // 1. 权限校验
        checkAdminRole(adminUserId);

        // 2. 不能禁用自己
        if (Objects.equals(userId, Long.valueOf(adminUserId))) {
            throw new RuntimeException("Cannot modify your own status");
        }

        // 3. 查询目标用户
        Users targetUser = lambdaQuery().eq(Users::getId, userId).one();
        if (targetUser == null) {
            throw new RuntimeException("User does not exist");
        }

        // 4. 切换状态：1→0, 0→1
        targetUser.setStatus(targetUser.getStatus() == 1 ? 0 : 1);
        updateById(targetUser);
        return targetUser.getStatus() == 1;
    }

    /**
     * 校验当前用户是否拥有管理员角色
     */
    private void checkAdminRole(Integer userId) {
        List<String> roles = userRolesService.getUserRoles(userId);
        if (roles == null || !roles.contains(RoleCode.ADMIN.getCode())) {
            throw new RuntimeException("Insufficient permissions: admin role required");
        }
    }

}
