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
 * 快捷回复表
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("quick_replies")
public class QuickReplies implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分类
     */
    private String category;

    /**
     * 快捷回复标题
     */
    private String title;

    /**
     * 快捷回复内容
     */
    private String content;

    /**
     * 关键词(逗号分隔)
     */
    private String keywords;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 使用次数
     */
    private Integer usageCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;


}
