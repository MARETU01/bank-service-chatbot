package com.maretu.user.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 设置/修改支付密码请求
 */
@Data
@Accessors(chain = true)
public class PayPasswordReq {
    /**
     * 支付密码（6位数字）
     */
    private String payPassword;

    /**
     * 旧支付密码（修改时需要）
     */
    private String oldPayPassword;
}
