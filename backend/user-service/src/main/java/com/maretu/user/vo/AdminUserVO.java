package com.maretu.user.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员用户列表返回对象
 */
@Data
public class AdminUserVO {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 角色代码列表
     */
    private List<String> roles;

    /**
     * 是否启用（status==1 → true）
     */
    private Boolean active;

    /**
     * 注册时间
     */
    private LocalDateTime createdAt;
}
