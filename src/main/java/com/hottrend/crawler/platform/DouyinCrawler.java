package com.hottrend.crawler.platform;

import com.hottrend.model.NewsItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 抖音热榜抓取器
 */
@Component
public class DouyinCrawler extends NewsNowCrawler {

    @Override
    public String getPlatformId() {
        return "douyin";
    }

    @Override
    public String getPlatformName() {
        return "抖音";
    }

    @Override
    public List<NewsItem> fetchTrending() throws Exception {
        return fetchFromNewsNow("douyin");
    }
}