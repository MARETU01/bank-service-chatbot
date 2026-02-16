package com.maretu.user.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户安全信息表
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_security")
public class UserSecurity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 安全问题
     */
    private String securityQuestion;

    /**
     * 安全答案
     */
    private String securityAnswer;

    /**
     * 是否启用双因素认证
     */
    private Boolean twoFactorEnabled;

    /**
     * 双因素认证密钥
     */
    private String twoFactorSecret;

    /**
     * 登录失败次数
     */
    private Integer failedLoginAttempts;

    /**
     * 锁定到期时间
     */
    private LocalDateTime lockedUntil;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
