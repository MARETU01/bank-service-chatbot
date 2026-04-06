package com.maretu.chat.service.impl;

import com.maretu.api.client.UserClient;
import com.maretu.chat.service.IKnowledgeService;
import com.maretu.common.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * <p>
 * 知识库文档管理 服务实现类
 * </p>
 *
 * @author maretu
 */
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements IKnowledgeService {

    private final VectorStore vectorStore;
    private final UserClient userClient;
    private final Executor virtualThreadPoolExecutor;

    /**
     * 检查用户是否具有 admin 角色
     */
    private void checkAdminRole(String userJson) {
        Result<List<String>> result = userClient.getUserRoles(userJson);
        if (result == null || result.getData() == null) {
            throw new RuntimeException("Failed to get user roles");
        }
        List<String> roles = result.getData();
        if (roles.isEmpty() || !roles.contains("ADMIN")) {
            throw new RuntimeException("Insufficient permissions: admin role required to perform this action");
        }
    }

    /**
     * 处理单个文件的入库逻辑
     */
    private Map<String, Object> processDocument(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Uploaded file cannot be empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new RuntimeException("Filename cannot be empty");
        }

        // 根据文件类型读取文档
        List<Document> documents;
        String fileType = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

        try {
            documents = switch (fileType) {
                case "pdf" -> readPdfDocument(file, filename);
                case "txt" -> readTextDocument(file, filename);
                default -> throw new RuntimeException("Unsupported file type: " + fileType + ", currently only PDF and TXT files are supported");
            };
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }

        if (documents.isEmpty()) {
            throw new RuntimeException("Document content is empty, cannot import to knowledge base");
        }

        // 文本切片
        TokenTextSplitter splitter = new TokenTextSplitter(
                800,    // defaultChunkSize: 每个切片的目标 token 数
                350,    // minChunkSizeChars: 最小切片字符数
                200,    // minChunkLengthToEmbed: 最小可嵌入的切片长度
                10000,  // maxNumChunks: 最大切片数量
                true    // keepSeparator: 保留分隔符
        );
        List<Document> chunks = splitter.apply(documents);

        // 写入向量数据库（VectorStore 会自动调用 Embedding 模型进行向量化）
        vectorStore.add(chunks);

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("filename", filename);
        resultData.put("fileType", fileType);
        resultData.put("originalDocCount", documents.size());
        resultData.put("chunkCount", chunks.size());

        return resultData;
    }

    @Override
    public List<Map<String, Object>> uploadDocuments(String userJson, MultipartFile[] files) {
        // 检查 admin 角色
        checkAdminRole(userJson);

        List<Map<String, Object>> results = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (MultipartFile file : files) {
            Map<String, Object> fileResult = new HashMap<>();
            fileResult.put("filename", file.getOriginalFilename());

            try {
                Map<String, Object> detail = processDocument(file);
                fileResult.put("status", "success");
                fileResult.put("detail", detail);
                successCount++;
            } catch (Exception e) {
                fileResult.put("status", "failed");
                fileResult.put("error", e.getMessage());
                failCount++;
            }
            results.add(fileResult);
        }

        if (successCount == 0) {
            throw new RuntimeException("All files failed to import to knowledge base");
        }

        return results;
    }

    @Override
    public void clearKnowledge(String userJson) {
        // 检查 admin 角色
        checkAdminRole(userJson);

        virtualThreadPoolExecutor.execute(() -> vectorStore.delete(new Filter.Expression(
                Filter.ExpressionType.EQ,
                new Filter.Key("type"),
                new Filter.Value("pdf")
        )));
    }

    /**
     * 读取 PDF 文档
     */
    private List<Document> readPdfDocument(MultipartFile file, String filename) throws IOException {
        PagePdfDocumentReader reader = new PagePdfDocumentReader(
                new InputStreamResource(file.getInputStream()),
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                        .withPagesPerDocument(1)
                        .build()
        );
        List<Document> documents = reader.read();

        // 为每个文档添加来源元数据
        documents.forEach(doc -> {
            doc.getMetadata().put("source", filename);
            doc.getMetadata().put("type", "pdf");
        });
        return documents;
    }

    /**
     * 读取纯文本文档
     */
    private List<Document> readTextDocument(MultipartFile file, String filename) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        if (content.isBlank()) {
            return List.of();
        }

        Document document = new Document(content);
        document.getMetadata().put("source", filename);
        document.getMetadata().put("type", "txt");
        return List.of(document);
    }
}
