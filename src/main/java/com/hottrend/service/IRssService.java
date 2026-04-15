package com.hottrend.service;

import com.hottrend.model.RssItem;
import com.hottrend.model.RssSource;

import java.util.List;

/**
 * RSS 服务接口
 */
public interface IRssService {

    // ===== RSS 源操作 =====

    /**
     * 获取所有 RSS 源
     */
    List<RssSource> listAllSources();

    /**
     * 获取启用的 RSS 源
     */
    List<RssSource> listEnabledSources();

    /**
     * 根据ID获取 RSS 源
     */
    RssSource getSourceById(String id);

    /**
     * 根据URL获取 RSS 源
     */
    RssSource getSourceByUrl(String url);

    /**
     * 添加 RSS 源
     */
    RssSource addSource(String name, String url, String groupName);

    /**
     * 更新 RSS 源
     */
    boolean updateSource(RssSource source);

    /**
     * 删除 RSS 源
     */
    boolean deleteSource(String id);

    // ===== RSS 文章操作 =====

    /**
     * 分页获取 RSS 文章
     */
    List<RssItem> listRssItems(String sourceId, int page, int pageSize);

    /**
     * 获取未推送的 RSS 文章
     */
    List<RssItem> getUnpushedItems();

    /**
     * 根据URL获取 RSS 文章
     */
    RssItem getRssItemByUrl(String url);

    /**
     * 根据订阅源获取文章
     */
    List<RssItem> getRssItemsBySourceId(String sourceId);

    // ===== 抓取操作 =====

    /**
     * 抓取所有 RSS 源
     */
    List<RssItem> fetchAllRss();

    /**
     * 抓取单个 RSS 源
     */
    List<RssItem> fetchRssSource(RssSource source) throws Exception;
}