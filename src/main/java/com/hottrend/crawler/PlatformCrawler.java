package com.hottrend.crawler;

import com.hottrend.model.NewsItem;
import java.util.List;

/**
 * 平台数据抓取器接口
 */
public interface PlatformCrawler {

    /**
     * 获取平台 ID
     */
    String getPlatformId();

    /**
     * 获取平台名称
     */
    String getPlatformName();

    /**
     * 抓取热榜数据
     *
     * @return 新闻列表
     */
    List<NewsItem> fetchTrending() throws Exception;
}