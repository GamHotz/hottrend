package com.hottrend.service.impl;

import com.hottrend.crawler.PlatformCrawler;
import com.hottrend.mapper.NewsMapper;
import com.hottrend.model.NewsItem;
import com.hottrend.model.Platform;
import com.hottrend.service.ICrawlerService;
import com.hottrend.service.IPlatformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * 抓取服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerServiceImpl implements ICrawlerService {
    private final Map<String, PlatformCrawler> crawlers;
    private final NewsMapper newsMapper;
    private final IPlatformService platformService;

    @Value("${platforms.enabled:true}")
    private boolean platformsEnabled;

    @Override
    public List<NewsItem> fetchAllPlatforms() {
        if (!platformsEnabled) {
            log.info("平台抓取已禁用");
            return Collections.emptyList();
        }

        // 通过注入的 PlatformService 获取平台列表
        List<Platform> enabledPlatforms = platformService.listEnabledPlatforms();

        List<NewsItem> allNews = new ArrayList<>();

        // 并发抓取所有平台
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, Math.min(enabledPlatforms.size(), 5), 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        List<Future<List<NewsItem>>> futures = enabledPlatforms.stream()
                .map(platform -> {
                    String pid = platform.getId() + "Crawler";
                    PlatformCrawler crawler = crawlers.get(pid);
                    if (crawler != null) {
                        return executor.submit(() -> {
                            try {
                                log.info("开始抓取平台: {}", platform.getName());
                                return crawler.fetchTrending();
                            } catch (Exception e) {
                                log.error("抓取 {} 失败: {}", platform.getName(), e.getMessage());
                                return Collections.<NewsItem>emptyList();
                            }
                        });
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();

        // 收集结果
        for (Future<List<NewsItem>> future : futures) {
            try {
                List<NewsItem> items = future.get(30, TimeUnit.SECONDS);
                allNews.addAll(items);
            } catch (Exception e) {
                log.error("获取抓取结果失败: {}", e.getMessage());
            }
        }
        executor.shutdown();

        // 去重并保存到数据库
        List<NewsItem> savedItems = deduplicateAndSave(allNews);

        log.info("抓取完成，共获取 {} 条新闻，保存 {} 条新数据", allNews.size(), savedItems.size());
        return savedItems;
    }

    @Override
    public List<NewsItem> fetchPlatform(String platformId) {
        PlatformCrawler crawler = crawlers.get(platformId);
        if (crawler == null) {
            log.warn("未找到平台抓取器: {}", platformId);
            return Collections.emptyList();
        }

        try {
            List<NewsItem> items = crawler.fetchTrending();
            return deduplicateAndSave(items);
        } catch (Exception e) {
            log.error("抓取平台 {} 失败: {}", platformId, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public void initPlatforms() {
        platformService.initDefaultPlatforms();
    }

    /**
     * 去重并保存新闻
     */
    private List<NewsItem> deduplicateAndSave(List<NewsItem> items) {
        List<NewsItem> savedItems = new ArrayList<>();

        for (NewsItem item : items) {
            // 根据 URL 去重
            NewsItem existing = newsMapper.findByUrl(item.getUrl());
            if (existing != null) {
                // 更新现有记录
                existing.setHotRank(item.getHotRank());
                existing.setHotValue(item.getHotValue());
                existing.setLastCrawlTime(LocalDateTime.now());
                newsMapper.updateById(existing);
            } else {
                // 保存新记录
                newsMapper.insert(item);
                savedItems.add(item);
            }
        }

        return savedItems;
    }
}