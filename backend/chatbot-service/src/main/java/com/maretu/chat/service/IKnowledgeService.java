package com.maretu.chat.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 知识库文档管理 服务类
 * </p>
 *
 * @author maretu
 */
public interface IKnowledgeService {

    /**
     * 批量上传文档到知识库
     *
     * @param files 上传的文件列表
     * @return 每个文件的入库结果
     */
    List<Map<String, Object>> uploadDocuments(MultipartFile[] files);

    /**
     * 清空知识库
     */
    void clearKnowledge();
}
