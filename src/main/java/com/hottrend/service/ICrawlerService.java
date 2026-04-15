package com.hottrend.service;

import com.hottrend.model.NewsItem;

import java.util.List;

/**
 * 抓取服务接口
 */
public interface ICrawlerService {

    /**
     * 抓取所有启用的平台数据
     */
    List<NewsItem> fetchAllPlatforms();

    /**
     * 抓取指定平台数据
     */
    List<NewsItem> fetchPlatform(String platformId);

    /**
     * 初始化平台配置
     */
    void initPlatforms();
}