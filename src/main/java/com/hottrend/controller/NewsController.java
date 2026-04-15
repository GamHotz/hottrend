package com.hottrend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hottrend.model.Keyword;
import com.hottrend.model.NewsItem;
import com.hottrend.model.dto.*;
import com.hottrend.service.IKeywordService;
import com.hottrend.service.INewsService;
import com.hottrend.service.INotificationService;
import com.hottrend.service.ISchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 新闻管理 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name = "新闻管理", description = "新闻数据查询、统计、抓取接口")
public class NewsController {

    private final INewsService newsService;
    private final IKeywordService keywordService;
    private final ISchedulerService schedulerService;
    private final INotificationService notificationService;

    @GetMapping
    @Operation(summary = "获取新闻列表", description = "分页查询新闻数据，支持按平台和关键词筛选")
    public ApiResponse<NewsListResponse> list(
            @Parameter(description = "平台ID") @RequestParam(required = false) String platformId,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {

        IPage<NewsItem> newsPage = newsService.listNews(platformId, keyword, page, pageSize);

        List<NewsDto> items = newsPage.getRecords().stream()
                .map(this::toDto)
                .toList();

        return ApiResponse.success(NewsListResponse.builder()
                .items(items)
                .total((int) newsPage.getTotal())
                .page(page)
                .pageSize(pageSize)
                .platformId(platformId)
                .keyword(keyword)
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取新闻详情", description = "根据ID获取新闻详情")
    public ApiResponse<NewsDto> get(
            @Parameter(description = "新闻ID") @PathVariable Long id) {
        NewsItem item = newsService.getNewsById(id);
        if (item != null) {
            return ApiResponse.success(toDto(item));
        }
        return ApiResponse.error("新闻不存在");
    }

    @GetMapping("/stats")
    @Operation(summary = "获取统计信息", description = "获取新闻总数、今日推送数、各平台统计等")
    public ApiResponse<StatsDto> stats() {
        Map<String, Long> platformStats = newsService.countByPlatform();
        Map<String, Long> keywordStats = new HashMap<>();
        List<Keyword> keywords = keywordService.listEnabledKeywords();
        keywords.forEach(kw -> keywordStats.put(kw.getKeyword(), 0L));

        return ApiResponse.success(StatsDto.builder()
                .totalNews(newsService.getTotalCount())
                .todayPushed(newsService.countTodayNews(LocalDateTime.now().with(java.time.LocalTime.MIN)))
                .platformStats(platformStats)
                .keywordStats(keywordStats)
                .build());
    }

    @GetMapping("/fetch")
    @Operation(summary = "手动抓取数据", description = "手动触发数据抓取任务")
    public ApiResponse<List<NewsDto>> fetch() {
        List<NewsItem> items = schedulerService.manualFetch();
        return ApiResponse.success("抓取完成", items.stream().map(this::toDto).toList());
    }

    @GetMapping("/clean")
    @Operation(summary = "清理旧数据", description = "清理旧数据")
    public ApiResponse<String> clean() {
        schedulerService.cleanupOldData();
        return ApiResponse.success("清理旧数据完成", null);
    }

    @PostMapping("/push")
    @Operation(summary = "手动推送", description = "手动触发新闻推送任务")
    public ApiResponse<String> push() {
        schedulerService.manualPush();
        return ApiResponse.success("推送完成", null);
    }

    @GetMapping("/unpushed")
    @Operation(summary = "获取未推送新闻", description = "获取所有未推送的新闻列表")
    public ApiResponse<List<NewsDto>> unpushed() {
        List<NewsItem> items = newsService.getUnpushedNews();
        return ApiResponse.success(items.stream().map(this::toDto).toList());
    }

    @PostMapping("/test-notification")
    @Operation(summary = "发送测试通知", description = "通过所有启用的渠道发送测试通知")
    public ApiResponse<String> testNotification(
            @Parameter(description = "通知渠道") @RequestParam String channel) {
        notificationService.sendToAllChannels(
                "测试通知",
                "这是来自 HotTrend 的测试消息"
        );
        return ApiResponse.success("测试通知已发送", null);
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
                .lastUpdateTime(item.getLastCrawlTime())
                .isPushed(item.getIsPushed())
                .tags(item.getTags())
                .aiScore(item.getAiScore())
                .isInteresting(item.getIsInteresting())
                .build();
    }
}