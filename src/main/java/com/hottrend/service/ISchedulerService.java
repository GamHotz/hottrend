package com.hottrend.service;

import com.hottrend.model.NewsItem;

import java.util.List;

/**
 * 调度服务接口
 */
public interface ISchedulerService {

    /**
     * 数据抓取任务
     */
    void fetchData();

    /**
     * 推送任务
     */
    void pushReport();

    /**
     * AI 分析任务
     */
    void aiAnalysis();

    /**
     * 清理旧数据
     */
    void cleanupOldData();

    /**
     * 手动触发抓取
     */
    List<NewsItem> manualFetch();

    /**
     * 手动触发推送
     */
    void manualPush();
}