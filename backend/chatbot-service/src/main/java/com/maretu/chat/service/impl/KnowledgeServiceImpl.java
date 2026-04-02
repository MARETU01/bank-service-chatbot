package com.maretu.chat.service.impl;

import com.maretu.chat.service.IKnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

    /**
     * 处理单个文件的入库逻辑
     */
    private Map<String, Object> processDocument(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new RuntimeException("文件名不能为空");
        }

        // 根据文件类型读取文档
        List<Document> documents;
        String fileType = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

        try {
            documents = switch (fileType) {
                case "pdf" -> readPdfDocument(file, filename);
                case "txt" -> readTextDocument(file, filename);
                default -> throw new RuntimeException("不支持的文件类型：" + fileType + "，目前仅支持 PDF 和 TXT 文件");
            };
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败：" + e.getMessage(), e);
        }

        if (documents.isEmpty()) {
            throw new RuntimeException("文档内容为空，无法入库");
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
            throw new RuntimeException("所有文件入库均失败");
        }

        return results;
    }

    @Override
    @Async("virtualThreadPoolExecutor")
    public void clearKnowledge(String userJson) {
        Filter.Expression filterExpression = new Filter.Expression(
                Filter.ExpressionType.EQ,
                new Filter.Key("type"),
                new Filter.Value("pdf")
        );
        vectorStore.delete(filterExpression);
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
