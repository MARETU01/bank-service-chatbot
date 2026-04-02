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

    List<Map<String, Object>> uploadDocuments(MultipartFile[] files);

    void clearKnowledge();
}
