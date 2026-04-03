package com.maretu.user.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 管理员分配角色请求参数
 */
@Data
@Accessors(chain = true)
public class AssignRolesReq {
    /**
     * 角色代码列表，如 ["USER", "ADMIN"]
     */
    private List<String> roles;
}
