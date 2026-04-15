package com.hottrend.crawler.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hottrend.crawler.PlatformCrawler;
import com.hottrend.model.NewsItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 NewsNow API 的平台数据抓取器基类
 * API 文档: https://github.com/ourongxing/newsnow
 */
@Slf4j
public abstract class NewsNowCrawler implements PlatformCrawler {

    protected final WebClient webClient;
    protected final ObjectMapper objectMapper;

    protected NewsNowCrawler() {
        this.webClient = WebClient.builder()
                .baseUrl("https://newsnow.busiyi.world/api/s")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 从 NewsNow API 抓取数据
     */
    protected List<NewsItem> fetchFromNewsNow(String platformId) {
        try {
            String response = webClient.get()
                    .uri("?id={platform}", platformId)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseNewsNowResponse(response, platformId);
        } catch (Exception e) {
            log.error("抓取 {} 数据失败: {}", platformId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 解析 NewsNow API 响应
     */
    protected List<NewsItem> parseNewsNowResponse(String response, String platformId) {
        List<NewsItem> items = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode itemsNode = root.get("items");

            if (itemsNode != null && itemsNode.isArray()) {
                int rank = 1;
                for (JsonNode node : itemsNode) {
                    String summary = null;
                    JsonNode extraNode = node.get("extra");
                    if (extraNode != null && !extraNode.isNull()) {
                        JsonNode hoverNode = extraNode.get("hover");
                        if (hoverNode != null && !hoverNode.isNull()) {
                            summary = hoverNode.asText();
                        }
                    }

                    NewsItem item = NewsItem.builder()
                            .title(getTextValue(node, "title"))
                            .url(getTextValue(node, "url"))
                            .hotRank(rank++)
                            .platformId(platformId)
                            .platformName(getPlatformName())
                            .summary(summary)
                            .firstCrawlTime(LocalDateTime.now())
                            .lastCrawlTime(LocalDateTime.now())
                            .isPushed(false)
                            .build();
                    items.add(item);
                }
            }
        } catch (Exception e) {
            log.error("解析 {} 响应失败: {}", platformId, e.getMessage());
        }

        return items;
    }

    /**
     * 获取文本值
     */
    protected String getTextValue(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null;
    }

    /**
     * 解析时间字符串
     */
    protected LocalDateTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return LocalDateTime.now();
        }
        // NewsNow 返回的是 Unix 时间戳
        try {
            long timestamp = Long.parseLong(timeStr);
            return LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochSecond(timestamp),
                    java.time.ZoneId.systemDefault()
            );
        } catch (NumberFormatException e) {
            return LocalDateTime.now();
        }
    }
}