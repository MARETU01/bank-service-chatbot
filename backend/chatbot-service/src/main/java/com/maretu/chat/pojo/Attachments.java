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
 * 附件表
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("attachments")
public class Attachments implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联消息ID
     */
    private Long messageId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 存储路径
     */
    private String storagePath;

    /**
     * 上传者类型: 1-用户, 2-客服
     */
    private Integer uploaderType;

    /**
     * 上传者ID
     */
    private Long uploaderId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;


}
