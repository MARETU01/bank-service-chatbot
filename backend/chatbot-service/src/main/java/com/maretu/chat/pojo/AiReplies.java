package com.maretu.chat.pojo;

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
 * 智能回复记录表
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("ai_replies")
public class AiReplies implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的消息ID
     */
    private Long messageId;

    /**
     * 用户问题
     */
    private String userQuestion;

    /**
     * AI回答
     */
    private String aiAnswer;

    /**
     * 置信度
     */
    private BigDecimal confidence;

    /**
     * 识别的意图
     */
    private String intent;

    /**
     * 是否被用户接受
     */
    private Boolean isAccepted;

    /**
     * 是否被人工覆盖
     */
    private Boolean manualOverride;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;


}
