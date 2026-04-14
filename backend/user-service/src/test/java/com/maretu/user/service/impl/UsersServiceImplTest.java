package com.maretu.user.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.maretu.common.utils.RedisConstants;
import com.maretu.user.dto.ResetPasswordReq;
import com.maretu.user.dto.UpdateProfileReq;
import com.maretu.user.enums.RoleCode;
import com.maretu.user.pojo.Users;
import com.maretu.user.service.IUserRolesService;
import com.maretu.user.utils.HashUtil;
import com.maretu.user.utils.MailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UsersServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsersServiceImpl 用户服务测试")
class UsersServiceImplTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private MailUtil mailUtil;

    @Mock
    private IUserRolesService userRolesService;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    @Spy
    private UsersServiceImpl usersService;

    // ==================== 辅助方法 ====================

    private Users buildUser(Long id, String email, String username, int status) {
        Users user = new Users();
        user.setId(id);
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(HashUtil.encodePassword("password123"));
        user.setStatus(status);
        user.setPhone("13800138000");
        user.setRealName("测试用户");
        return user;
    }

    @SuppressWarnings("unchecked")
    private LambdaQueryChainWrapper<Users> mockLambdaQuery() {
        LambdaQueryChainWrapper<Users> wrapper = mock(LambdaQueryChainWrapper.class);
        doReturn(wrapper).when(usersService).lambdaQuery();
        lenient().when(wrapper.eq(any(), any())).thenReturn(wrapper);
        lenient().when(wrapper.like(any(), any())).thenReturn(wrapper);
        lenient().when(wrapper.or()).thenReturn(wrapper);
        return wrapper;
    }

    // ==================== 登录测试 ====================

    @Nested
    @DisplayName("登录功能")
    class LoginTest {

        @Test
        @DisplayName("邮箱和手机号都为空 - 应抛出异常")
        void noEmailNoPhone_shouldThrowException() {
            Users user = new Users();
            user.setEmail(null);
            user.setPhone(null);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.login(user, "127.0.0.1"));
            assertEquals("email or phone is required", ex.getMessage());
        }

        @Test
        @DisplayName("邮箱登录 - 用户不存在 - 应抛出异常")
        void emailLogin_userNotExist_shouldThrowException() {
            Users loginReq = new Users();
            loginReq.setEmail("notexist@example.com");
            loginReq.setPassword("password123");

            LambdaQueryChainWrapper<Users> wrapper = mockLambdaQuery();
            when(wrapper.one()).thenReturn(null);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.login(loginReq, "127.0.0.1"));
            assertEquals("email/phone or password not correct", ex.getMessage());
        }

        @Test
        @DisplayName("邮箱登录 - 密码错误 - 应抛出异常")
        void emailLogin_wrongPassword_shouldThrowException() {
            Users loginReq = new Users();
            loginReq.setEmail("test@example.com");
            loginReq.setPassword("wrongPassword");

            Users dbUser = buildUser(1L, "test@example.com", "testUser", 1);

            LambdaQueryChainWrapper<Users> wrapper = mockLambdaQuery();
            when(wrapper.one()).thenReturn(dbUser);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.login(loginReq, "127.0.0.1"));
            assertEquals("email/phone or password not correct", ex.getMessage());
        }

        @Test
        @DisplayName("邮箱登录 - 用户被禁用 - 应抛出异常")
        void emailLogin_userDisabled_shouldThrowException() {
            Users loginReq = new Users();
            loginReq.setEmail("test@example.com");
            loginReq.setPassword("password123");

            Users dbUser = buildUser(1L, "test@example.com", "testUser", 0); // 禁用

            LambdaQueryChainWrapper<Users> wrapper = mockLambdaQuery();
            when(wrapper.one()).thenReturn(dbUser);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.login(loginReq, "127.0.0.1"));
            assertEquals("user's status is not active", ex.getMessage());
        }

        @Test
        @DisplayName("邮箱登录 - 正常登录 - 应返回JWT")
        void emailLogin_success_shouldReturnJwt() {
            Users loginReq = new Users();
            loginReq.setEmail("test@example.com");
            loginReq.setPassword("password123");

            Users dbUser = buildUser(1L, "test@example.com", "testUser", 1);

            LambdaQueryChainWrapper<Users> wrapper = mockLambdaQuery();
            when(wrapper.one()).thenReturn(dbUser);

            // mock self 调用（loginLog 是 @Async 方法）
            // 由于 self 是通过 @Autowired 注入的，在测试中我们需要设置它
            // 使用 spy 来处理
            doNothing().when(usersService).loginLog(any(Users.class), anyString());

            String jwt = usersService.login(loginReq, "127.0.0.1");

            assertNotNull(jwt);
            assertFalse(jwt.isBlank());
        }
    }

    // ==================== 获取当前用户 ====================

    @Nested
    @DisplayName("获取当前用户")
    class GetCurrentUserTest {

        @Test
        @DisplayName("用户不存在 - 应抛出异常")
        void userNotExist_shouldThrowException() {
            LambdaQueryChainWrapper<Users> wrapper = mockLambdaQuery();
            when(wrapper.one()).thenReturn(null);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.getCurrentUser(999));
            assertEquals("user not found", ex.getMessage());
        }

        @Test
        @DisplayName("正常获取 - 密码应被置空")
        void normalGet_passwordShouldBeNull() {
            Users dbUser = buildUser(1L, "test@example.com", "testUser", 1);

            LambdaQueryChainWrapper<Users> wrapper = mockLambdaQuery();
            when(wrapper.one()).thenReturn(dbUser);

            Users result = usersService.getCurrentUser(1);
            assertNull(result.getPassword());
            assertEquals("test@example.com", result.getEmail());
        }
    }

    // ==================== 重置密码 ====================

    @Nested
    @DisplayName("重置密码")
    class ResetPasswordTest {

        @Test
        @DisplayName("请求为null - 应抛出异常")
        void nullRequest_shouldThrowException() {
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.resetPassword(null, "123456"));
            assertEquals("request is required", ex.getMessage());
        }

        @Test
        @DisplayName("邮箱为空 - 应抛出异常")
        void emptyEmail_shouldThrowException() {
            ResetPasswordReq req = new ResetPasswordReq().setEmail("").setNewPassword("newPwd");

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.resetPassword(req, "123456"));
            assertEquals("email is required", ex.getMessage());
        }

        @Test
        @DisplayName("验证码为空 - 应抛出异常")
        void emptyVerifyCode_shouldThrowException() {
            ResetPasswordReq req = new ResetPasswordReq()
                    .setEmail("test@example.com")
                    .setNewPassword("newPwd");

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.resetPassword(req, ""));
            assertEquals("verifyCode is required", ex.getMessage());
        }

        @Test
        @DisplayName("新密码为空 - 应抛出异常")
        void emptyNewPassword_shouldThrowException() {
            ResetPasswordReq req = new ResetPasswordReq()
                    .setEmail("test@example.com")
                    .setNewPassword("");

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.resetPassword(req, "123456"));
            assertEquals("newPassword is required", ex.getMessage());
        }

        @Test
        @DisplayName("用户不存在 - 应抛出异常")
        void userNotExist_shouldThrowException() {
            ResetPasswordReq req = new ResetPasswordReq()
                    .setEmail("notexist@example.com")
                    .setNewPassword("newPwd123");

            LambdaQueryChainWrapper<Users> wrapper = mockLambdaQuery();
            when(wrapper.one()).thenReturn(null);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.resetPassword(req, "123456"));
            assertEquals("user not found", ex.getMessage());
        }

        @Test
        @DisplayName("用户被禁用 - 应抛出异常")
        void userDisabled_shouldThrowException() {
            ResetPasswordReq req = new ResetPasswordReq()
                    .setEmail("test@example.com")
                    .setNewPassword("newPwd123");

            Users dbUser = buildUser(1L, "test@example.com", "testUser", 0);

            LambdaQueryChainWrapper<Users> wrapper = mockLambdaQuery();
            when(wrapper.one()).thenReturn(dbUser);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.resetPassword(req, "123456"));
            assertEquals("user's status is not active", ex.getMessage());
        }
    }

    // ==================== 更新个人资料 ====================

    @Nested
    @DisplayName("更新个人资料")
    class UpdateProfileTest {

        @Test
        @DisplayName("userId为null - 应抛出异常")
        void nullUserId_shouldThrowException() {
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.updateProfile(null, new UpdateProfileReq()));
            assertEquals("user not found", ex.getMessage());
        }

        @Test
        @DisplayName("用户不存在 - 应抛出异常")
        void userNotExist_shouldThrowException() {
            LambdaQueryChainWrapper<Users> wrapper = mockLambdaQuery();
            when(wrapper.one()).thenReturn(null);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.updateProfile(999, new UpdateProfileReq()));
            assertEquals("user not found", ex.getMessage());
        }

        @Test
        @DisplayName("用户被禁用 - 应抛出异常")
        void userDisabled_shouldThrowException() {
            Users dbUser = buildUser(1L, "test@example.com", "testUser", 0);

            LambdaQueryChainWrapper<Users> wrapper = mockLambdaQuery();
            when(wrapper.one()).thenReturn(dbUser);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.updateProfile(1, new UpdateProfileReq()));
            assertEquals("user's status is not active", ex.getMessage());
        }

        @Test
        @DisplayName("无变更 - 应返回当前用户信息且密码为空")
        void noChange_shouldReturnCurrentUser() {
            Users dbUser = buildUser(1L, "test@example.com", "testUser", 1);

            LambdaQueryChainWrapper<Users> wrapper = mockLambdaQuery();
            when(wrapper.one()).thenReturn(dbUser);

            // 不设置任何更新字段
            UpdateProfileReq req = new UpdateProfileReq();
            Users result = usersService.updateProfile(1, req);

            assertNull(result.getPassword());
            assertEquals("testUser", result.getUsername());
        }

        @Test
        @DisplayName("用户名已被占用 - 应抛出异常")
        void usernameAlreadyTaken_shouldThrowException() {
            Users dbUser = buildUser(1L, "test@example.com", "testUser", 1);
            Users existingUser = buildUser(2L, "other@example.com", "newUsername", 1);

            LambdaQueryChainWrapper<Users> wrapper = mockLambdaQuery();
            // 第一次查询返回当前用户，第二次查询返回已存在的用户（用户名查重）
            when(wrapper.one()).thenReturn(dbUser).thenReturn(existingUser);

            UpdateProfileReq req = new UpdateProfileReq().setUsername("newUsername");

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.updateProfile(1, req));
            assertEquals("username already registered", ex.getMessage());
        }
    }

    // ==================== 切换用户状态 ====================

    @Nested
    @DisplayName("切换用户状态（管理员功能）")
    class ToggleUserStatusTest {

        @Test
        @DisplayName("非管理员 - 应抛出权限异常")
        void nonAdmin_shouldThrowException() {
            when(userRolesService.getUserRoles(1)).thenReturn(List.of("USER"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.toggleUserStatus(2L, 1));
            assertTrue(ex.getMessage().contains("admin role required"));
        }

        @Test
        @DisplayName("禁用自己 - 应抛出异常")
        void disableSelf_shouldThrowException() {
            when(userRolesService.getUserRoles(1)).thenReturn(List.of("ADMIN"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.toggleUserStatus(1L, 1));
            assertEquals("Cannot modify your own status", ex.getMessage());
        }

        @Test
        @DisplayName("目标用户不存在 - 应抛出异常")
        void targetUserNotExist_shouldThrowException() {
            when(userRolesService.getUserRoles(1)).thenReturn(List.of("ADMIN"));

            LambdaQueryChainWrapper<Users> wrapper = mockLambdaQuery();
            when(wrapper.one()).thenReturn(null);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.toggleUserStatus(999L, 1));
            assertEquals("User does not exist", ex.getMessage());
        }

        @Test
        @DisplayName("正常切换状态 - 活跃→禁用")
        void toggleActiveToDisabled_shouldSucceed() {
            when(userRolesService.getUserRoles(1)).thenReturn(List.of("ADMIN"));

            Users targetUser = buildUser(2L, "target@example.com", "targetUser", 1);

            LambdaQueryChainWrapper<Users> wrapper = mockLambdaQuery();
            when(wrapper.one()).thenReturn(targetUser);
            doReturn(true).when(usersService).updateById(any(Users.class));

            Boolean result = usersService.toggleUserStatus(2L, 1);
            assertFalse(result); // 1→0，返回false
            assertEquals(0, targetUser.getStatus());
        }

        @Test
        @DisplayName("正常切换状态 - 禁用→活跃")
        void toggleDisabledToActive_shouldSucceed() {
            when(userRolesService.getUserRoles(1)).thenReturn(List.of("ADMIN"));

            Users targetUser = buildUser(2L, "target@example.com", "targetUser", 0);

            LambdaQueryChainWrapper<Users> wrapper = mockLambdaQuery();
            when(wrapper.one()).thenReturn(targetUser);
            doReturn(true).when(usersService).updateById(any(Users.class));

            Boolean result = usersService.toggleUserStatus(2L, 1);
            assertTrue(result); // 0→1，返回true
            assertEquals(1, targetUser.getStatus());
        }
    }

    // ==================== 发送验证码 ====================

    @Nested
    @DisplayName("发送验证码")
    class SendCodeTest {

        @Test
        @DisplayName("不支持的类型 - 应抛出异常")
        void unsupportedType_shouldThrowException() {
            Users user = new Users();
            user.setEmail("test@example.com");

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.sendCode(user, "unknown"));
            assertTrue(ex.getMessage().contains("unsupported type"));
        }

        @Test
        @DisplayName("验证码已发送（未过期） - 应抛出异常")
        void codeAlreadySent_shouldThrowException() {
            Users user = new Users();
            user.setEmail("test@example.com");

            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any()))
                    .thenReturn(false); // 已存在

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.sendCode(user, "register"));
            assertTrue(ex.getMessage().contains("already sent"));
        }
    }

    // ==================== 刷新Token ====================

    @Nested
    @DisplayName("刷新Token")
    class RefreshTest {

        @Test
        @DisplayName("用户不存在 - 应抛出异常")
        void userNotExist_shouldThrowException() {
            LambdaQueryChainWrapper<Users> wrapper = mockLambdaQuery();
            when(wrapper.one()).thenReturn(null);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.refresh(999, "127.0.0.1"));
            assertEquals("user not found", ex.getMessage());
        }

        @Test
        @DisplayName("用户被禁用 - 应抛出异常")
        void userDisabled_shouldThrowException() {
            Users dbUser = buildUser(1L, "test@example.com", "testUser", 0);

            LambdaQueryChainWrapper<Users> wrapper = mockLambdaQuery();
            when(wrapper.one()).thenReturn(dbUser);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> usersService.refresh(1, "127.0.0.1"));
            assertEquals("user's status is not active", ex.getMessage());
        }

        @Test
        @DisplayName("正常刷新 - 应返回新JWT")
        void normalRefresh_shouldReturnNewJwt() {
            Users dbUser = buildUser(1L, "test@example.com", "testUser", 1);

            LambdaQueryChainWrapper<Users> wrapper = mockLambdaQuery();
            when(wrapper.one()).thenReturn(dbUser);
            doNothing().when(usersService).loginLog(any(Users.class), anyString());

            String jwt = usersService.refresh(1, "127.0.0.1");
            assertNotNull(jwt);
            assertFalse(jwt.isBlank());
        }
    }
}
