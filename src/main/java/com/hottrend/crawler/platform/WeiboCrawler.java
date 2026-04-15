package com.hottrend.crawler.platform;

import com.hottrend.model.NewsItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 微博热榜抓取器
 */
@Component
public class WeiboCrawler extends NewsNowCrawler {

    @Override
    public String getPlatformId() {
        return "weibo";
    }

    @Override
    public String getPlatformName() {
        return "微博";
    }

    @Override
    public List<NewsItem> fetchTrending() throws Exception {
        return fetchFromNewsNow("weibo");
    }
}