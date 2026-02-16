package com.maretu.chat.pojo;

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
 * 聊天会话表
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_sessions")
public class ChatSessions implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话唯一标识
     */
    private String sessionId;

    /**
     * 用户ID(关联用户服务)
     */
    private Long userId;

    /**
     * 会话类型: 1-普通咨询, 2-业务办理, 3-投诉建议, 4-紧急求助
     */
    private Integer sessionType;

    /**
     * 状态: 0-已关闭, 1-进行中, 2-等待回复
     */
    private Integer status;

    /**
     * 优先级: 1-普通, 2-紧急, 3-非常紧急
     */
    private Integer priority;

    /**
     * 会话主题
     */
    private String topic;

    /**
     * 会话摘要
     */
    private String summary;

    /**
     * 分配给哪个客服人员
     */
    private Long assignedTo;

    /**
     * 满意度评分: 1-5分
     */
    private Integer satisfactionScore;

    /**
     * 用户反馈
     */
    private String feedback;

    /**
     * 关闭时间
     */
    private LocalDateTime closedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;


}
