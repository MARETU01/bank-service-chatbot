package com.maretu.user.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 管理员查询用户列表请求参数
 */
@Data
@Accessors(chain = true)
public class AdminUserQuery {
    /**
     * 搜索关键词（用户名/邮箱模糊匹配）
     */
    private String keyword;

    /**
     * 角色筛选（角色代码，如 ADMIN）
     */
    private String role;

    /**
     * 页码，默认1
     */
    private Integer page = 1;

    /**
     * 每页条数，默认10
     */
    private Integer size = 10;
}
