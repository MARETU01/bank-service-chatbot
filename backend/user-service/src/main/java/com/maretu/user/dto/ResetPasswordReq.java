package com.maretu.user.dto;

import lombok.Data;

/**
 * 忘记密码-重置密码请求
 */
@Data
public class ResetPasswordReq {
    private String email;

    private String verifyCode;

    private String newPassword;
}
