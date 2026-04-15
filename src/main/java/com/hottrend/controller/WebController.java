package com.hottrend.controller;

import com.hottrend.model.NewsItem;
import com.hottrend.service.IHtmlReportService;
import com.hottrend.service.INewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Web 页面控制器
 */
@Controller
@RequiredArgsConstructor
public class WebController {

    private final INewsService newsService;
    private final IHtmlReportService htmlReportService;

    /**
     * 首页 - 新闻列表
     */
    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(required = false) String platform,
                        @RequestParam(required = false) String keyword) {
        List<NewsItem> news;

        if (keyword != null && !keyword.isEmpty()) {
            // 搜索
            var page = newsService.listNews(null, keyword, 1, 100);
            news = page.getRecords();
        } else if (platform != null && !platform.isEmpty()) {
            // 按平台筛选
            news = newsService.getNewsByPlatformAndTimeRange(platform, LocalDateTime.now().minusDays(1));
        } else {
            // 获取最近的新闻
            news = newsService.getNewsByTimeRange(LocalDateTime.now().minusDays(1), LocalDateTime.now());
        }

        // 按平台分组
        Map<String, List<NewsItem>> groupedNews = news.stream()
                .collect(Collectors.groupingBy(NewsItem::getPlatformName));

        model.addAttribute("groupedNews", groupedNews);
        model.addAttribute("updateTime", LocalDateTime.now());
        return "index";
    }

    /**
     * HTML 报告页面
     */
    @GetMapping("/report")
    public String report(Model model, @RequestParam(required = false) String platform) {
        List<NewsItem> news;

        if (platform != null && !platform.isEmpty()) {
            news = newsService.getNewsByPlatformAndTimeRange(platform, LocalDateTime.now().minusDays(1));
        } else {
            news = newsService.getNewsByTimeRange(LocalDateTime.now().minusDays(1), LocalDateTime.now());
        }

        String html = htmlReportService.generateNewsHtml(news);
        model.addAttribute("htmlContent", html);

        return "report";
    }

    /**
     * API 文档
     */
    @GetMapping("/api-docs")
    public String apiDocs() {
        return "redirect:/swagger-ui.html";
    }
}