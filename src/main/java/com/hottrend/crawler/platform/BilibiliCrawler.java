package com.hottrend.crawler.platform;

import com.hottrend.model.NewsItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * B站热搜抓取器
 */
@Component
public class BilibiliCrawler extends NewsNowCrawler {

    @Override
    public String getPlatformId() {
        return "bilibili-hot-search";
    }

    @Override
    public String getPlatformName() {
        return "B站";
    }

    @Override
    public List<NewsItem> fetchTrending() throws Exception {
        return fetchFromNewsNow("bilibili-hot-search");
    }
}