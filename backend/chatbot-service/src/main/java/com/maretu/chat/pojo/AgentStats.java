package com.maretu.chat.pojo;

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
 * 客服工作统计表
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("agent_stats")
public class AgentStats implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 客服人员ID
     */
    private Long agentId;

    /**
     * 客服人员名称
     */
    private String agentName;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 处理会话总数
     */
    private Integer totalSessions;

    /**
     * 已关闭会话数
     */
    private Integer closedSessions;

    /**
     * 处理消息总数
     */
    private Integer totalMessages;

    /**
     * 平均响应时间(秒)
     */
    private Integer avgResponseTime;

    /**
     * 平均满意度
     */
    private BigDecimal satisfactionAvg;

    /**
     * 工作时长(小时)
     */
    private BigDecimal workHours;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;


}
