package com.hottrend.service.impl;

import com.hottrend.model.NewsItem;
import com.hottrend.service.IHtmlReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HTML 报告生成服务实现类
 */
@Slf4j
@Service
public class HtmlReportServiceImpl implements IHtmlReportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String generateNewsHtml(List<NewsItem> newsItems) {
        if (newsItems == null || newsItems.isEmpty()) {
            return generateEmptyHtml("暂无新闻数据");
        }

        // 按平台分组
        Map<String, List<NewsItem>> groupedByPlatform = newsItems.stream()
                .collect(Collectors.groupingBy(NewsItem::getPlatformName));

        StringBuilder html = new StringBuilder();

        html.append("""
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>热点雷达 - 热点新闻</title>
                <style>
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; background: #f5f7fa; padding: 20px; line-height: 1.6; }
                    .container { max-width: 900px; margin: 0 auto; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 12px; margin-bottom: 24px; text-align: center; }
                    .header h1 { font-size: 28px; margin-bottom: 8px; }
                    .header p { opacity: 0.9; font-size: 14px; }
                    .platform-section { background: white; border-radius: 12px; padding: 20px; margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
                    .platform-title { font-size: 18px; font-weight: 600; color: #333; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 2px solid #667eea; }
                    .news-item { display: flex; align-items: flex-start; padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
                    .news-item:last-child { border-bottom: none; }
                    .news-rank { min-width: 32px; height: 32px; background: #667eea; color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 600; margin-right: 12px; flex-shrink: 0; }
                    .news-content { flex: 1; }
                    .news-title { color: #333; text-decoration: none; font-size: 15px; font-weight: 500; display: block; }
                    .news-title:hover { color: #667eea; }
                    .news-meta { font-size: 12px; color: #999; margin-top: 4px; }
                    .news-hot { color: #ff6b6b; font-size: 12px; margin-left: 8px; }
                    .badge-new { background: #52c41a; color: white; font-size: 10px; padding: 2px 6px; border-radius: 4px; margin-left: 8px; }
                    .footer { text-align: center; padding: 20px; color: #999; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔥 热点雷达</h1>
                        <p>更新时间: %s</p>
                    </div>
            """.formatted(LocalDateTime.now().format(DATE_FORMATTER)));

        // 生成各平台新闻
        for (Map.Entry<String, List<NewsItem>> entry : groupedByPlatform.entrySet()) {
            html.append("<div class='platform-section'>");
            html.append("<div class='platform-title'>").append(entry.getKey()).append("</div>");

            int rank = 1;
            for (NewsItem item : entry.getValue()) {
                boolean isNew = item.getFirstCrawlTime() != null && item.getFirstCrawlTime().isAfter(LocalDateTime.now().minusHours(6));

                html.append("""
                    <div class="news-item">
                        <div class="news-rank">%d</div>
                        <div class="news-content">
                            <a href="%s" class="news-title" target="_blank">%s%s</a>
                            <div class="news-meta">
                                %s%s
                            </div>
                        </div>
                    </div>
                """.formatted(
                        rank++,
                        escapeHtml(item.getUrl() != null ? item.getUrl() : "#"),
                        escapeHtml(item.getTitle()),
                        isNew ? "<span class='badge-new'>NEW</span>" : "",
                        item.getPlatformName() != null ? item.getPlatformName() + " · " : "",
                        item.getHotValue() != null ? "<span class='news-hot'>" + escapeHtml(item.getHotValue()) + "</span>" : ""
                ));
            }

            html.append("</div>");
        }

        html.append("""
            <div class="footer">
                Generated by HotTrend Spring Edition
            </div>
            </div>
            </body>
            </html>
            """);

        return html.toString();
    }

    @Override
    public String generateEmptyHtml(String message) {
        return """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <title>热点雷达</title>
                <style>
                    body { font-family: -apple-system, sans-serif; padding: 40px; text-align: center; }
                </style>
            </head>
            <body>
                <h1>%s</h1>
            </body>
            </html>
            """.formatted(message);
    }

    /**
     * HTML 转义
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}