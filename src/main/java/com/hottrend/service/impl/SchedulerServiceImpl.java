package com.hottrend.service.impl;

import com.hottrend.model.NewsItem;
import com.hottrend.model.Keyword;
import com.hottrend.model.RssItem;
import com.hottrend.model.dto.NewsDto;
import com.hottrend.model.dto.PushReportDto;
import com.hottrend.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 调度服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerServiceImpl implements ISchedulerService {

    private final ICrawlerService crawlerService;
    private final IRssService rssService;
    private final INewsService newsService;
    private final IKeywordService keywordService;
    private final INotificationService notificationService;

    @Value("${scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Value("${scheduler.preset:morning_evening}")
    private String preset;

    @Value("${ai.enabled:false}")
    private boolean aiEnabled;

    // 注入 AI 服务（如果启用）
    private com.hottrend.ai.AIService aiService;

    // 预设时间配置
    private static final Map<String, SchedulePreset> PRESETS = Map.of(
            "always_on", new SchedulePreset(true, true, 0),
            "morning_evening", new SchedulePreset(true, true, 30),
            "office_hours", new SchedulePreset(true, true, 60),
            "night_owl", new SchedulePreset(true, true, 90)
    );

    @Override
    @Scheduled(cron = "0 0 * * * *")
    public void fetchData() {
        if (!schedulerEnabled) return;

        log.info("开始执行数据抓取任务...");

        try {
            // 抓取热榜数据
            List<NewsItem> platformNews = crawlerService.fetchAllPlatforms();
            log.info("热榜数据抓取完成: {} 条", platformNews.size());

            // 抓取 RSS 数据
            List<RssItem> rssItems = rssService.fetchAllRss();
            log.info("RSS 数据抓取完成: {} 条", rssItems.size());
        } catch (Exception e) {
            log.error("数据抓取任务失败: {}", e.getMessage());
        }
    }

    @Override
    @Scheduled(cron = "0 0 8,18 * * *")
    public void pushReport() {
        if (!schedulerEnabled) return;

        log.info("开始执行推送任务...");

        try {
            // 获取未推送的新闻
            List<NewsItem> unpushedNews = newsService.getUnpushedNews();

            if (unpushedNews.isEmpty()) {
                log.info("没有需要推送的新闻");
                return;
            }

            // 应用关键词过滤
            List<Keyword> keywords = keywordService.listEnabledKeywords();
            List<NewsItem> filteredNews = filterByKeywords(unpushedNews, keywords);

            // AI 智能筛选（如果启用）
            if (aiEnabled && aiService != null && aiService.isAvailable()) {
                filteredNews = applyAIFilter(filteredNews);
            }

            if (filteredNews.isEmpty()) {
                log.info("筛选后没有符合条件的数据");
                return;
            }

            // 构建推送报告
            PushReportDto report = buildReport(filteredNews);

            // 发送通知
            notificationService.sendReport(report);

            // 标记为已推送
            for (NewsItem item : filteredNews) {
                item.setIsPushed(true);
                newsService.updateNews(item);
            }

            log.info("推送完成: {} 条新闻", filteredNews.size());

        } catch (Exception e) {
            log.error("推送任务失败: {}", e.getMessage());
        }
    }

    @Override
    @Scheduled(cron = "0 30 20 * * *")
    public void aiAnalysis() {
        if (!schedulerEnabled || !aiEnabled || aiService == null) return;

        log.info("开始执行 AI 分析任务...");

        try {
            LocalDateTime today = LocalDateTime.now().with(LocalTime.MIN);
            List<NewsItem> todayNews = newsService.getNewsByTimeRange(today, LocalDateTime.now());

            if (!todayNews.isEmpty()) {
                var result = aiService.analyzeNews(todayNews);
                if (result != null) {
                    log.info("AI 分析完成: {}", result.getSummary());
                }
            }
        } catch (Exception e) {
            log.error("AI 分析任务失败: {}", e.getMessage());
        }
    }

    @Override
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOldData() {
        if (!schedulerEnabled) return;
        log.info("开始清理旧数据...");

        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(3);
            newsService.cleanupOldData(cutoff);
            // 清理逻辑（根据需求实现）
            log.info("数据清理完成");
        } catch (Exception e) {
            log.error("数据清理失败: {}", e.getMessage());
        }
    }

    @Override
    public List<NewsItem> manualFetch() {
        List<NewsItem> platformNews = crawlerService.fetchAllPlatforms();
        rssService.fetchAllRss();
        return platformNews;
    }

    @Override
    public void manualPush() {
        pushReport();
    }

    /**
     * 关键词过滤
     */
    private List<NewsItem> filterByKeywords(List<NewsItem> news, List<Keyword> keywords) {
        if (keywords.isEmpty()) {
            return news;
        }

        return news.stream()
                .filter(item -> keywords.stream().anyMatch(kw ->
                        matchesKeyword(item.getTitle(), kw.getKeyword(), kw.getMatchMode()) ||
                                matchesKeyword(item.getSummary(), kw.getKeyword(), kw.getMatchMode())
                ))
                .collect(Collectors.toList());
    }

    private boolean matchesKeyword(String text, String keyword, String mode) {
        if (text == null || text.isEmpty()) return false;

        return switch (mode) {
            case "exact" -> text.equals(keyword);
            case "regex" -> text.matches(keyword);
            default -> text.contains(keyword);
        };
    }

    /**
     * AI 智能筛选
     */
    private List<NewsItem> applyAIFilter(List<NewsItem> news) {
        try {
            var result = aiService.filterByInterest(news, "科技、数码、AI相关的新闻");
            if (result != null && result.getScores() != null) {
                return news.stream()
                        .filter(item -> result.getScores().getOrDefault(item.getId(), 0.0) >= 0.5)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("AI 筛选失败，回退到关键词过滤: {}", e.getMessage());
        }
        return news;
    }

    /**
     * 构建推送报告
     */
    private PushReportDto buildReport(List<NewsItem> newsItems) {
        // 按平台分组
        Map<String, List<NewsDto>> grouped = newsItems.stream()
                .map(this::toDto)
                .collect(Collectors.groupingBy(NewsDto::getPlatformName));

        // 新增新闻（今天首次出现的）
        LocalDateTime today = LocalDateTime.now().with(LocalTime.MIN);
        List<NewsItem> todayNewItems = newsItems.stream()
                .filter(n -> n.getFirstCrawlTime().isAfter(today))
                .toList();

        // 转换为 DTO
        Map<String, List<NewsDto>> groupedDtos = grouped.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().limit(10).collect(Collectors.toList())
                ));

        return PushReportDto.builder()
                .title("热点雷达 - " + LocalDateTime.now().toLocalDate())
                .groupedNews(groupedDtos)
                .newItems(todayNewItems.stream().map(this::toDto).collect(Collectors.toList()))
                .totalCount(newsItems.size())
                .reportTime(LocalDateTime.now())
                .build();
    }

    private NewsDto toDto(NewsItem item) {
        return NewsDto.builder()
                .id(item.getId())
                .title(item.getTitle())
                .url(item.getUrl())
                .hotRank(item.getHotRank())
                .hotValue(item.getHotValue())
                .platformId(item.getPlatformId())
                .platformName(item.getPlatformName())
                .summary(item.getSummary())
                .publishTime(item.getPublishTime())
                .firstSeenTime(item.getFirstCrawlTime())
                .isPushed(item.getIsPushed())
                .build();
    }

    /**
     * 调度预设配置
     */
    private record SchedulePreset(boolean fetchEnabled, boolean pushEnabled, int intervalMinutes) {}
}