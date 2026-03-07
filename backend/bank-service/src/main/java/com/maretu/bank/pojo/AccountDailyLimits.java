package com.maretu.bank.pojo;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 账户日限额使用表
 * </p>
 *
 * @author maretu
 * @since 2026-03-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("account_daily_limits")
public class AccountDailyLimits implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 限额日期
     */
    private LocalDate limitDate;

    /**
     * 当日限额快照(来自accounts.daily_limit)
     */
    private BigDecimal dailyLimit;

    /**
     * 当日已用额度(支出累计)
     */
    private BigDecimal usedAmount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;


}
