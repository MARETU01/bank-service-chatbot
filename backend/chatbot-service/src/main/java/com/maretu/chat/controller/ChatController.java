package com.maretu.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maretu.chat.pojo.Message;
import com.maretu.chat.pojo.Session;
import com.maretu.chat.service.IMessageService;
import com.maretu.chat.service.ISessionService;
import com.maretu.common.dto.Context;
import com.maretu.common.utils.Result;
import com.maretu.common.utils.VectorDistanceUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ISessionService sessionService;
    private final IMessageService messageService;
    private final ObjectMapper jacksonObjectMapper;
    private final OpenAiEmbeddingModel openAiEmbeddingModel;

    @GetMapping("/session")
    public Result<List<Session>> getSessions(@RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            return Result.success(sessionService.getSessions(context.getUserId()));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PostMapping("/session")
    public Result<Session> createSession(@RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            return Result.success(sessionService.createSession(context.getUserId()));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @DeleteMapping("/session/{sessionId}")
    public Result<Boolean> deleteSession(@PathVariable("sessionId") String sessionId,
                                      @RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            return Result.success(sessionService.deleteSession(context.getUserId(), sessionId));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PutMapping("/session/{sessionId}")
    public Result<Boolean> renameSession(@RequestHeader("user-info") String userJson,
                                         @PathVariable("sessionId") String sessionId,
                                         @RequestBody Session session) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            return Result.success(sessionService.renameSession(context.getUserId(), sessionId, session));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @GetMapping("/message/{sessionId}")
    public Result<List<Message>> getSessionMessages(@PathVariable("sessionId") String sessionId,
                                                    @RequestHeader("user-info") String userJson) throws JsonProcessingException {
        Context context = jacksonObjectMapper.readValue(userJson, Context.class);
        try {
            return Result.success(messageService.getMessages(context.getUserId(), sessionId));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @PostMapping(produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestHeader("user-info") String userJson,
                             @RequestBody Message message) throws JsonProcessingException {
        return messageService.chat(userJson, message);
    }

    @GetMapping("/test")
    public Result<Void> test() {
            // 1.测试数据
            // 1.1.用来查询的文本，国际冲突
            String query = "global conflicts";

            // 1.2.用来做比较的文本
            String[] texts = new String[]{
                    "哈马斯称加沙下阶段停火谈判仍在进行 以方尚未做出承诺",
                    "土耳其、芬兰、瑞典与北约代表将继续就瑞典“入约”问题进行谈判",
                    "日本航空基地水井中检测出有机氟化物超标",
                    "国家游泳中心（水立方）：恢复游泳、嬉水乐园等水上项目运营",
                    "我国首次在空间站开展舱外辐射生物学暴露实验",
            };
            // 2.向量化
            // 2.1.先将查询文本向量化
            float[] queryVector = openAiEmbeddingModel.embed(query);

            // 2.2.再将比较文本向量化，放到一个数组
            List<float[]> textVectors = openAiEmbeddingModel.embed(Arrays.asList(texts));

            // 3.比较欧氏距离
            // 3.1.把查询文本自己与自己比较，肯定是相似度最高的
            System.out.println(VectorDistanceUtils.euclideanDistance(queryVector, queryVector));
            // 3.2.把查询文本与其它文本比较
            for (float[] textVector : textVectors) {
                System.out.println(VectorDistanceUtils.euclideanDistance(queryVector, textVector));
            }
            System.out.println("------------------");

            // 4.比较余弦距离
            // 4.1.把查询文本自己与自己比较，肯定是相似度最高的
            System.out.println(VectorDistanceUtils.cosineDistance(queryVector, queryVector));
            // 4.2.把查询文本与其它文本比较
            for (float[] textVector : textVectors) {
                System.out.println(VectorDistanceUtils.cosineDistance(queryVector, textVector));
            }
        return Result.success(null);
    }
}
