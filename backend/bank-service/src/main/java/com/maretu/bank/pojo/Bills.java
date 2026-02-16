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
 * 账单表
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("bills")
public class Bills implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 账单类型: CREDIT_CARD-信用卡, LOAN-贷款, SERVICE-服务费
     */
    private String billType;

    /**
     * 账单编号
     */
    private String billNumber;

    /**
     * 账单金额
     */
    private BigDecimal billAmount;

    /**
     * 已付金额
     */
    private BigDecimal paidAmount;

    /**
     * 未付金额
     */
    private BigDecimal outstandingAmount;

    /**
     * 到期日期
     */
    private LocalDate dueDate;

    /**
     * 账单日期
     */
    private LocalDate billDate;

    /**
     * 状态: 0-未付, 1-部分支付, 2-已付, 3-逾期
     */
    private Integer status;

    /**
     * 账单描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;


}
