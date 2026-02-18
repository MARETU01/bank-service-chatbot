package com.maretu.common.utils;

import org.springframework.util.StringUtils;

/**
 * 脱敏工具类（对外展示用）。
 * <p>
 * 约定：
 * 1) 传入 null/blank 返回原值（null/""）。
 * 2) 尽量不抛异常；无法识别格式时，退化为通用掩码。
 */
public class DesensitizeUtils {

    /**
     * 真实姓名脱敏。
     * <ul>
     *     <li>长度=1：返回"*"</li>
     *     <li>长度=2：保留首字："张*"</li>
     *     <li>长度>=3：保留首尾："张*三"</li>
     * </ul>
     */
    public static String maskRealName(String realName) {
        if (!StringUtils.hasText(realName)) {
            return realName;
        }
        String name = realName.trim();
        int len = name.length();
        if (len == 1) {
            return "*";
        }
        if (len == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + repeat('*', len - 2) + name.charAt(len - 1);
    }

    /**
     * 手机号脱敏。
     * <p>
     * 优先按常见 11 位手机号显示：前3后4（138****1234）。
     * 其他情况：保留首1末1，中间掩码。
     */
    public static String maskPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return phone;
        }
        String raw = phone.trim();
        // 尽量只取数字（允许 +86、空格、短横线等）
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() == 11) {
            return digits.substring(0, 3) + "****" + digits.substring(7);
        }
        return maskKeep(raw, 1, 1, '*');
    }

    /**
     * 邮箱脱敏。
     * <p>
     * 例：alice@example.com -&gt; a***e@example.com
     */
    public static String maskEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return email;
        }
        String raw = email.trim();
        int at = raw.indexOf('@');
        if (at <= 0 || at == raw.length() - 1) {
            // 非法邮箱，退化处理
            return maskKeep(raw, 1, 1, '*');
        }
        String local = raw.substring(0, at);
        String domain = raw.substring(at);

        if (local.length() <= 1) {
            return "*" + domain;
        }
        if (local.length() == 2) {
            return local.charAt(0) + "*" + domain;
        }
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + domain;
    }

    /**
     * 通用掩码：保留开头 keepStart 个字符与结尾 keepEnd 个字符，其余用 maskChar 替换。
     */
    public static String maskKeep(String raw, int keepStart, int keepEnd, char maskChar) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        String s = raw.trim();
        int len = s.length();
        if (keepStart < 0) {
            keepStart = 0;
        }
        if (keepEnd < 0) {
            keepEnd = 0;
        }
        if (keepStart + keepEnd >= len) {
            return repeat(maskChar, len);
        }
        String start = keepStart == 0 ? "" : s.substring(0, keepStart);
        String end = keepEnd == 0 ? "" : s.substring(len - keepEnd);
        return start + repeat(maskChar, len - keepStart - keepEnd) + end;
    }

    private static String repeat(char c, int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}

