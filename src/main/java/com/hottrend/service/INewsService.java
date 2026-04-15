package com.hottrend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hottrend.model.NewsItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 新闻服务接口
 */
public interface INewsService {

    /**
     * 分页查询新闻列表
     */
    IPage<NewsItem> listNews(String platformId, String keyword, int page, int pageSize);

    /**
     * 根据ID获取新闻
     */
    NewsItem getNewsById(Long id);

    /**
     * 保存新闻
     */
    boolean saveNews(NewsItem newsItem);

    /**
     * 批量保存新闻
     */
    List<NewsItem> saveBatchNews(List<NewsItem> newsItems);

    /**
     * 更新新闻
     */
    boolean updateNews(NewsItem newsItem);

    /**
     * 根据URL查找新闻（用于去重）
     */
    NewsItem getNewsByUrl(String url);

    /**
     * 查找未推送的新闻
     */
    List<NewsItem> getUnpushedNews();

    /**
     * 根据时间范围查找新闻
     */
    List<NewsItem> getNewsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据平台和时间范围查找新闻
     */
    List<NewsItem> getNewsByPlatformAndTimeRange(String platformId, LocalDateTime startTime);

    /**
     * 统计今日新闻数
     */
    Long countTodayNews(LocalDateTime date);

    /**
     * 统计各平台新闻数
     */
    Map<String, Long> countByPlatform();

    /**
     * 获取新闻总数
     */
    Long getTotalCount();

    void cleanupOldData(LocalDateTime cutoff);
}