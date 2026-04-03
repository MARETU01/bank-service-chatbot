package com.maretu.user.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 验证支付密码请求
 */
@Data
@Accessors(chain = true)
public class VerifyPayPasswordReq {
    /**
     * 支付密码（6位数字）
     */
    private String payPassword;
}
