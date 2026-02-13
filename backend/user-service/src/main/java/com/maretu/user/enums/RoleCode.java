package com.maretu.user.enums;

import lombok.Getter;

/**
 * 角色代码常量
 *
 * @author maretu
 * @since 2025-04-23
 */
@Getter
public enum RoleCode {
    /**
     * 普通用户
     */
    USER("USER", "普通用户"),
    /**
     * 管理员
     */
    ADMIN("ADMIN", "管理员"),
    /**
     * 客服
     */
    CUSTOMER_SERVICE("CUSTOMER_SERVICE", "客服");

    private final String code;
    private final String desc;

    RoleCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
