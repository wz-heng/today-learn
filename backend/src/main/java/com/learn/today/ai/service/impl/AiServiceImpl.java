package com.learn.today.ai.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.today.ai.dto.AiTaskResult;
import com.learn.today.ai.service.AiService;
import com.learn.today.common.exception.BusinessException;
import com.learn.today.topic.entity.Topic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${app.ai.default-model:deepseek-chat}")
    private String defaultModel;

    @Value("${app.ai.claude.api-key:}")
    private String claudeApiKey;

    @Value("${app.ai.claude.api-url:https://api.anthropic.com/v1/messages}")
    private String claudeApiUrl;

    @Value("${app.ai.deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${app.ai.deepseek.api-url:https://api.deepseek.com/v1/chat/completions}")
    private String deepseekApiUrl;

    // -------------------------------------------------------
    // 主入口
    // -------------------------------------------------------

    @Override
    public List<AiTaskResult> generateTasks(int availableMinutes,
                                            List<Topic> topics,
                                            List<String> learnedTitles,
                                            String model) {
        String resolvedModel = (model == null || model.isBlank()) ? defaultModel : model;
        String prompt = buildPrompt(availableMinutes, topics, learnedTitles);
        log.debug("使用模型: {}，生成任务 prompt 长度: {}", resolvedModel, prompt.length());

        String responseJson = isDeepSeek(resolvedModel)
                ? callDeepSeekApi(prompt, resolvedModel)
                : callClaudeApi(prompt, resolvedModel);

        return parseTasksFromResponse(responseJson, resolvedModel);
    }

    // -------------------------------------------------------
    // Prompt 构建
    // -------------------------------------------------------

    private String buildPrompt(int availableMinutes, List<Topic> topics, List<String> learnedTitles) {
        String topicNames = topics.stream().map(Topic::getName).collect(Collectors.joining("、"));
        String topicSlugs = topics.stream()
                .map(t -> t.getSlug() + "（" + t.getName() + "）")
                .collect(Collectors.joining("、"));
        String learnedPart = learnedTitles.isEmpty()
                ? "无"
                : learnedTitles.stream().limit(30).collect(Collectors.joining("、"));

        return String.format("""
                你是一名专业技术讲师，请为用户生成今日学习任务，每个任务需包含完整的学习资料。

                【学习方向】：%s
                【今日可用时间】：%d 分钟
                【已学过的内容，请勿重复】：%s

                任务数量要求：
                - 时间 ≤ 15 分钟：1-2 条
                - 时间 ≤ 30 分钟：2-3 条
                - 时间 > 30 分钟：3-5 条
                - 所有任务 estimatedMinutes 之和不超过可用时间

                content 字段要求（这是核心，必须写详细）：
                - 包含知识点的核心概念解释
                - 包含至少 1 个具体示例或代码片段（如适用）
                - 包含重点/注意事项
                - 长度：200-500字，排版清晰，可用换行分段
                - 不要只写"去学什么"，要直接把知识点教给用户

                topicSlug 必须从以下值中选择：%s

                请直接返回 JSON 数组，不要有任何其他文字、注释或 markdown 标记：
                [
                  {
                    "title": "任务标题（15字以内）",
                    "content": "完整学习资料，包含概念讲解、示例、注意事项",
                    "difficulty": "easy 或 medium 或 hard",
                    "estimatedMinutes": 预计分钟数（整数）,
                    "topicSlug": "对应方向的英文slug"
                  }
                ]
                """,
                topicNames, availableMinutes, learnedPart, topicSlugs);
    }

    // -------------------------------------------------------
    // Claude API（Anthropic 格式）
    // -------------------------------------------------------

    private String callClaudeApi(String prompt, String model) {
        if (claudeApiKey.isBlank()) {
            throw new BusinessException("Claude API Key 未配置");
        }
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 4096);
        requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        try {
            return restClient.post()
                    .uri(claudeApiUrl)
                    .header("x-api-key", claudeApiKey)
                    .header("anthropic-version", "2023-06-01")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            log.error("调用 Claude API 失败: {}", e.getMessage());
            throw new BusinessException("AI 服务暂时不可用：" + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // DeepSeek API（OpenAI 兼容格式）
    // -------------------------------------------------------

    private String callDeepSeekApi(String prompt, String model) {
        if (deepseekApiKey.isBlank()) {
            throw new BusinessException("DeepSeek API Key 未配置");
        }
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        requestBody.put("temperature", 0.7);

        try {
            return restClient.post()
                    .uri(deepseekApiUrl)
                    .header("Authorization", "Bearer " + deepseekApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            log.error("调用 DeepSeek API 失败: {}", e.getMessage());
            throw new BusinessException("AI 服务暂时不可用：" + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // 解析响应
    // -------------------------------------------------------

    private List<AiTaskResult> parseTasksFromResponse(String responseJson, String model) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            String text;

            if (isDeepSeek(model)) {
                // OpenAI 格式：choices[0].message.content
                text = root.path("choices").get(0).path("message").path("content").asText();
            } else {
                // Claude 格式：content[0].text
                text = root.path("content").get(0).path("text").asText();
            }

            // AI 有时会在 JSON 外包 ```json ... ``` 标记，清理掉
            text = text.replaceAll("(?s)```json\\s*", "").replaceAll("```", "").trim();

            List<AiTaskResult> tasks = objectMapper.readValue(text, new TypeReference<>() {});
            if (tasks == null || tasks.isEmpty()) {
                throw new BusinessException("AI 未能生成学习任务，请重试");
            }
            log.info("AI ({}) 生成任务 {} 条", model, tasks.size());
            return tasks;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析 AI 响应失败，原始内容: {}", responseJson, e);
            throw new BusinessException("AI 返回格式异常，请重试");
        }
    }

    private boolean isDeepSeek(String model) {
        return model != null && model.toLowerCase().startsWith("deepseek");
    }
}
