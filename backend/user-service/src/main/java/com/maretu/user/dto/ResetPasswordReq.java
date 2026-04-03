package com.maretu.user.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 忘记密码 - 重置密码请求
 */
@Data
@Accessors(chain = true)
public class ResetPasswordReq {
    private String email;

    private String newPassword;
}
