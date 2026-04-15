package com.hottrend.ai;

import com.hottrend.model.NewsItem;
import java.util.List;

/**
 * AI 服务接口
 */
public interface AIService {

    /**
     * 分析新闻列表
     *
     * @param newsList 新闻列表
     * @return 分析结果
     */
    AIAnalysisResult analyzeNews(List<NewsItem> newsList);

    /**
     * 根据用户兴趣筛选新闻
     *
     * @param newsList    新闻列表
     * @param userInterest 用户兴趣描述
     * @return 筛选结果
     */
    AIFilterResult filterByInterest(List<NewsItem> newsList, String userInterest);

    /**
     * 翻译文本
     *
     * @param text          原文
     * @param targetLanguage 目标语言
     * @return 翻译结果
     */
    AITranslationResult translate(String text, String targetLanguage);

    /**
     * 检查 AI 是否可用
     */
    boolean isAvailable();
}