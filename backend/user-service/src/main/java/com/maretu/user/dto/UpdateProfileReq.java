package com.maretu.user.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 更新个人资料请求（仅允许更新非敏感字段）
 */
@Data
@Accessors(chain = true)
public class UpdateProfileReq {
    private String username;
    private String phone;
    private String realName;
}
