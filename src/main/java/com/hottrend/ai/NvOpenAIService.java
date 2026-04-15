package com.hottrend.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hottrend.model.NewsItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

/**
 * AI 服务实现（基于 OpenAI 兼容 API）
 */
@Slf4j
@Service
public class NvOpenAIService implements AIService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.enabled:false}")
    private boolean enabled;

    @Value("${spring.ai.model.options.model:deepseek-ai/deepseek-v3.2}")
    private String model;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:https://integrate.api.nvidia.com/v1}")
    private String apiBase;

    public NvOpenAIService() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public AIAnalysisResult analyzeNews(List<NewsItem> newsList) {
        if (!isAvailable() || newsList == null || newsList.isEmpty()) {
            return null;
        }

        try {
            String prompt = buildAnalysisPrompt(newsList);

            String response = callAI(prompt);
            return parseAnalysisResponse(response, newsList);
        } catch (Exception e) {
            log.error("AI 分析失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public AIFilterResult filterByInterest(List<NewsItem> newsList, String userInterest) {
        if (!isAvailable() || newsList == null || newsList.isEmpty()) {
            return AIFilterResult.builder()
                    .matchedNewsIds(Collections.emptyList())
                    .scores(Collections.emptyMap())
                    .extractedTags(Collections.emptyList())
                    .build();
        }

        try {
            String prompt = buildFilterPrompt(newsList, userInterest);

            String response = callAI(prompt);
            return parseFilterResponse(response, newsList);
        } catch (Exception e) {
            log.error("AI 筛选失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public AITranslationResult translate(String text, String targetLanguage) {
        if (!isAvailable() || text == null || text.isEmpty()) {
            return null;
        }

        try {
            String prompt = String.format(
                    "Translate the following text to %s. Only return the translated text, nothing else:\n\n%s",
                    targetLanguage, text
            );

            String response = callAI(prompt);

            return AITranslationResult.builder()
                    .originalText(text)
                    .translatedText(response.trim())
                    .targetLanguage(targetLanguage)
                    .build();
        } catch (Exception e) {
            log.error("AI 翻译失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean isAvailable() {
        return enabled && apiKey != null && !apiKey.isEmpty();
    }

    private String callAI(String prompt) {
        String requestBody = String.format("""
            {
                "model": "%s",
                "messages": [
                    {"role": "system", "content": "You are a helpful assistant."},
                    {"role": "user", "content": "%s"}
                ],
                "temperature": 0.7
            }
            """, model, prompt.replace("\"", "\\\""));

        String response = webClient.post()
                .uri(apiBase + "/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // 解析响应
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                return choices.get(0).get("message").get("content").asText();
            }
        } catch (Exception e) {
            log.error("解析 AI 响应失败: {}", e.getMessage());
        }

        return "";
    }

    private String buildAnalysisPrompt(List<NewsItem> newsList) {
        StringBuilder sb = new StringBuilder();
        sb.append("Please analyze the following trending news and provide:\n");
        sb.append("1. A brief summary (2-3 sentences)\n");
        sb.append("2. Key categories with scores (0-1)\n");
        sb.append("3. Main trends (3-5 points)\n");
        sb.append("4. Controversial or hot topics (if any)\n");
        sb.append("5. Strategic suggestions\n\n");
        sb.append("News list:\n");

        for (int i = 0; i < Math.min(newsList.size(), 20); i++) {
            NewsItem item = newsList.get(i);
            sb.append(String.format("%d. [%s] %s - %s\n",
                    i + 1,
                    item.getPlatformName(),
                    item.getTitle(),
                    item.getHotValue() != null ? item.getHotValue() : ""));
        }

        sb.append("\nPlease respond in JSON format:\n");
        sb.append("{\"summary\": \"...\", \"categories\": {\"tech\": 0.8, ...}, \"trends\": [...], \"controversies\": [...], \"suggestions\": [...]}");

        return sb.toString();
    }

    private String buildFilterPrompt(List<NewsItem> newsList, String userInterest) {
        StringBuilder sb = new StringBuilder();
        sb.append("Based on the user's interest: \"").append(userInterest).append("\"\n\n");
        sb.append("Please score each news item from 0-1 based on relevance to the user's interest.\n\n");
        sb.append("News list:\n");

        for (int i = 0; i < Math.min(newsList.size(), 30); i++) {
            NewsItem item = newsList.get(i);
            sb.append(String.format("ID:%d | [%s] %s\n",
                    item.getId(),
                    item.getPlatformName(),
                    item.getTitle()));
        }

        sb.append("\nPlease respond in JSON format:\n");
        sb.append("{\"matched_ids\": [id1, id2, ...], \"scores\": {id1: 0.9, id2: 0.8, ...}, \"tags\": [\"tag1\", \"tag2\", ...]}");

        return sb.toString();
    }

    private AIAnalysisResult parseAnalysisResponse(String response, List<NewsItem> newsList) {
        try {
            // 提取 JSON 部分
            int start = response.indexOf("{");
            int end = response.lastIndexOf("}");
            if (start >= 0 && end >= 0) {
                String jsonStr = response.substring(start, end + 1);
                JsonNode root = objectMapper.readTree(jsonStr);

                String summary = root.has("summary") ? root.get("summary").asText() : "";
                Map<String, Double> categories = new HashMap<>();
                if (root.has("categories")) {
                    JsonNode cats = root.get("categories");
                    cats.fields().forEachRemaining(e ->
                            categories.put(e.getKey(), e.getValue().asDouble()));
                }

                List<String> trends = new ArrayList<>();
                if (root.has("trends")) {
                    root.get("trends").forEach(n -> trends.add(n.asText()));
                }

                List<String> controversies = new ArrayList<>();
                if (root.has("controversies")) {
                    root.get("controversies").forEach(n -> controversies.add(n.asText()));
                }

                List<String> suggestions = new ArrayList<>();
                if (root.has("suggestions")) {
                    root.get("suggestions").forEach(n -> suggestions.add(n.asText()));
                }

                return AIAnalysisResult.builder()
                        .summary(summary)
                        .categoryScores(categories)
                        .trends(trends)
                        .controversies(controversies)
                        .suggestions(suggestions)
                        .build();
            }
        } catch (Exception e) {
            log.error("解析分析结果失败: {}", e.getMessage());
        }

        return null;
    }

    private AIFilterResult parseFilterResponse(String response, List<NewsItem> newsList) {
        try {
            int start = response.indexOf("{");
            int end = response.lastIndexOf("}");
            if (start >= 0 && end >= 0) {
                String jsonStr = response.substring(start, end + 1);
                JsonNode root = objectMapper.readTree(jsonStr);

                List<Long> matchedIds = new ArrayList<>();
                if (root.has("matched_ids")) {
                    root.get("matched_ids").forEach(n -> matchedIds.add(n.asLong()));
                }

                Map<Long, Double> scores = new HashMap<>();
                if (root.has("scores")) {
                    JsonNode scoreNodes = root.get("scores");
                    scoreNodes.fields().forEachRemaining(e ->
                            scores.put(Long.parseLong(e.getKey()), e.getValue().asDouble()));
                }

                List<String> tags = new ArrayList<>();
                if (root.has("tags")) {
                    root.get("tags").forEach(n -> tags.add(n.asText()));
                }

                return AIFilterResult.builder()
                        .matchedNewsIds(matchedIds)
                        .scores(scores)
                        .extractedTags(tags)
                        .build();
            }
        } catch (Exception e) {
            log.error("解析筛选结果失败: {}", e.getMessage());
        }

        return null;
    }
}