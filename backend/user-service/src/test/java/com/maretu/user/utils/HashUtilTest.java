package com.maretu.user.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HashUtil 单元测试
 */
@DisplayName("HashUtil 密码工具类测试")
class HashUtilTest {

    @Test
    @DisplayName("加密密码 - 应返回非空且与原文不同的字符串")
    void encodePassword_shouldReturnEncodedString() {
        String raw = "myPassword123";
        String encoded = HashUtil.encodePassword(raw);

        assertNotNull(encoded);
        assertFalse(encoded.isBlank());
        assertNotEquals(raw, encoded);
    }

    @Test
    @DisplayName("加密密码 - 同一密码每次加密结果应不同（BCrypt盐值）")
    void encodePassword_samePwdShouldProduceDifferentHash() {
        String raw = "myPassword123";
        String encoded1 = HashUtil.encodePassword(raw);
        String encoded2 = HashUtil.encodePassword(raw);

        assertNotEquals(encoded1, encoded2);
    }

    @Test
    @DisplayName("校验密码 - 正确密码应返回true")
    void checkPassword_correctPwdShouldReturnTrue() {
        String raw = "myPassword123";
        String encoded = HashUtil.encodePassword(raw);

        assertTrue(HashUtil.checkPassword(raw, encoded));
    }

    @Test
    @DisplayName("校验密码 - 错误密码应返回false")
    void checkPassword_wrongPwdShouldReturnFalse() {
        String raw = "myPassword123";
        String encoded = HashUtil.encodePassword(raw);

        assertFalse(HashUtil.checkPassword("wrongPassword", encoded));
    }

    @Test
    @DisplayName("校验密码 - 空密码应返回false")
    void checkPassword_emptyPwdShouldReturnFalse() {
        String encoded = HashUtil.encodePassword("myPassword123");

        assertFalse(HashUtil.checkPassword("", encoded));
    }

    @Test
    @DisplayName("加密密码 - 特殊字符密码应正常工作")
    void encodePassword_specialCharsShouldWork() {
        String raw = "P@$$w0rd!#%^&*()_+中文密码";
        String encoded = HashUtil.encodePassword(raw);

        assertNotNull(encoded);
        assertTrue(HashUtil.checkPassword(raw, encoded));
    }

    @Test
    @DisplayName("加密密码 - 长密码应正常工作")
    void encodePassword_longPasswordShouldWork() {
        String raw = "a".repeat(100);
        String encoded = HashUtil.encodePassword(raw);

        assertNotNull(encoded);
        assertTrue(HashUtil.checkPassword(raw, encoded));
    }
}
