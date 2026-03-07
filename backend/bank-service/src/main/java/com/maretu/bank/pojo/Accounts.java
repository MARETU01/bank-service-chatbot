package com.maretu.bank.pojo;

import java.math.BigDecimal;
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
 * 账户表
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("accounts")
public class Accounts implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 账号
     */
    private String accountNumber;

    /**
     * 用户ID(关联用户服务)
     */
    private Long userId;

    /**
     * 账户类型: 1-储蓄账户, 2-支票账户, 3-信用卡
     */
    private Integer accountType;

    /**
     * 账户余额
     */
    private BigDecimal balance;

    /**
     * 货币类型
     */
    private String currency;

    /**
     * 状态: 0-冻结, 1-正常, 2-关闭
     */
    private Integer status;

    /**
     * 日转账限额
     */
    private BigDecimal dailyTransferLimit;

    /**
     * 日取现限额
     */
    private BigDecimal dailyWithdrawLimit;

    /**
     * 单笔转账限额
     */
    private BigDecimal singleTransferLimit;

    /**
     * 单笔取现限额
     */
    private BigDecimal singleWithdrawLimit;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

}
