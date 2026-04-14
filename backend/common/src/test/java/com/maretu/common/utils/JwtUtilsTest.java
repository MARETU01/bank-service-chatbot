package com.maretu.common.utils;

import com.maretu.common.dto.Context;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtils 单元测试
 */
@DisplayName("JwtUtils 工具类测试")
class JwtUtilsTest {

    private Context buildContext() {
        Context context = new Context();
        context.setUserId(1)
                .setUsername("testUser")
                .setEmail("test@example.com")
                .setIp("127.0.0.1");
        return context;
    }

    @Test
    @DisplayName("生成JWT - 应返回非空字符串")
    void generateJwt_shouldReturnNonEmptyString() {
        Context context = buildContext();
        String jwt = JwtUtils.generateJwt(context);

        assertNotNull(jwt);
        assertFalse(jwt.isBlank());
    }

    @Test
    @DisplayName("解析JWT - 应正确还原Context信息")
    void parseJwt_shouldRestoreContextCorrectly() {
        Context original = buildContext();
        String jwt = JwtUtils.generateJwt(original);

        Context parsed = JwtUtils.parseJwt(jwt);

        assertEquals(original.getUserId(), parsed.getUserId());
        assertEquals(original.getUsername(), parsed.getUsername());
        assertEquals(original.getEmail(), parsed.getEmail());
        assertEquals(original.getIp(), parsed.getIp());
    }

    @Test
    @DisplayName("解析JWT - 不同Context应生成不同JWT")
    void generateJwt_differentContextShouldProduceDifferentJwt() {
        Context context1 = buildContext();
        Context context2 = new Context();
        context2.setUserId(2)
                .setUsername("anotherUser")
                .setEmail("another@example.com")
                .setIp("192.168.1.1");

        String jwt1 = JwtUtils.generateJwt(context1);
        String jwt2 = JwtUtils.generateJwt(context2);

        assertNotEquals(jwt1, jwt2);
    }

    @Test
    @DisplayName("解析过期JWT - 应抛出ExpiredJwtException")
    void parseJwt_expiredTokenShouldThrowException() {
        // 手动构造一个已过期的JWT
        SecretKey signingKey = Keys.hmacShaKeyFor(
                "maretumaretumaretumaretumaretuaa".getBytes(StandardCharsets.UTF_8));

        String expiredJwt = Jwts.builder()
                .claim("userId", 1)
                .claim("username", "testUser")
                .claim("email", "test@example.com")
                .claim("ip", "127.0.0.1")
                .signWith(signingKey)
                .expiration(new Date(System.currentTimeMillis() - 1000)) // 已过期
                .compact();

        assertThrows(ExpiredJwtException.class, () -> JwtUtils.parseJwt(expiredJwt));
    }

    @Test
    @DisplayName("解析无效JWT - 应抛出异常")
    void parseJwt_invalidTokenShouldThrowException() {
        assertThrows(Exception.class, () -> JwtUtils.parseJwt("invalid.jwt.token"));
    }

    @Test
    @DisplayName("logOutJwt - 应返回有效JWT")
    void logOutJwt_shouldReturnValidJwt() {
        Context context = buildContext();
        String jwt = JwtUtils.logOutJwt(context);

        assertNotNull(jwt);
        assertFalse(jwt.isBlank());

        // 应该可以正常解析
        Context parsed = JwtUtils.parseJwt(jwt);
        assertEquals(context.getUserId(), parsed.getUserId());
    }
}
