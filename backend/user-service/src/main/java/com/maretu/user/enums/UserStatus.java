package com.maretu.user.enums;

import lombok.Getter;

/**
 * 用户状态枚举
 *
 * @author maretu
 * @since 2025-04-23
 */
@Getter
public enum UserStatus {
    /**
     * 禁用
     */
    DISABLED(0, "禁用"),
    /**
     * 正常
     */
    NORMAL(1, "正常");

    private final Integer code;
    private final String desc;

    UserStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UserStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
