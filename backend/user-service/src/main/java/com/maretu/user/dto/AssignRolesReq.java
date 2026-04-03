package com.maretu.user.dto;

import lombok.Data;

import java.util.List;

/**
 * 管理员分配角色请求参数
 */
@Data
public class AssignRolesReq {
    /**
     * 角色代码列表，如 ["USER", "ADMIN"]
     */
    private List<String> roles;
}
