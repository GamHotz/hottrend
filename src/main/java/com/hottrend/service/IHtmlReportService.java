package com.hottrend.service;

import com.hottrend.model.NewsItem;

import java.util.List;

/**
 * 报告服务接口
 */
public interface IHtmlReportService {

    /**
     * 生成新闻列表 HTML
     */
    String generateNewsHtml(List<NewsItem> newsItems);

    /**
     * 生成空报告 HTML
     */
    String generateEmptyHtml(String message);
}