package com.hottrend.service;

import com.hottrend.model.dto.ContentResult;

/**
 * 内容爬取服务接口
 */
public interface IContentCrawlerService {

    /**
     * 根据URL获取Markdown格式的内容
     * @param url 文章URL
     * @return Markdown格式的内容
     */
    ContentResult getContentAsMarkdown(String url);

    /**
     * 获取纯文本摘要（用于新闻摘要）
     * @param url 文章URL
     * @return 摘要文本
     */
    String getSummary(String url);
}