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
 * 转账记录表
 * </p>
 *
 * @author maretu
 * @since 2026-03-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("transfers")
public class Transfers implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 转账 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 转账流水号
     */
    private String transferId;

    /**
     * 转出账户 ID
     */
    private Long fromAccountId;

    /**
     * 转入账户 ID
     */
    private Long toAccountId;

    /**
     * 转入账号
     */
    private String toAccountNumber;

    /**
     * 转入账户名
     */
    private String toAccountName;

    /**
     * 转账金额
     */
    private BigDecimal amount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态: 0-失败, 1-成功, 2-处理中
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;


}
