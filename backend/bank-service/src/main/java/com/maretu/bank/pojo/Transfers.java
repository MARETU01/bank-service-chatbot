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
 * @since 2026-02-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("transfers")
public class Transfers implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 转账ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 转账ID
     */
    private String transferId;

    /**
     * 转出账户ID
     */
    private Long fromAccountId;

    /**
     * 转入账户ID
     */
    private Long toAccountId;

    /**
     * 转出账号
     */
    private String fromAccountNumber;

    /**
     * 转入账号
     */
    private String toAccountNumber;

    /**
     * 转入账户名称
     */
    private String toAccountName;

    /**
     * 转入银行名称
     */
    private String toBankName;

    /**
     * 转账金额
     */
    private BigDecimal amount;

    /**
     * 手续费
     */
    private BigDecimal fee;

    /**
     * 货币类型
     */
    private String currency;

    /**
     * 转账类型: 1-行内转账, 2-跨行转账
     */
    private Integer transferType;

    /**
     * 转账备注
     */
    private String remark;

    /**
     * 状态: 0-失败, 1-成功, 2-处理中
     */
    private Integer status;

    /**
     * 转账时间
     */
    private LocalDateTime transferTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;


}
